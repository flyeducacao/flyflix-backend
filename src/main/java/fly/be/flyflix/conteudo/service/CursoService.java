package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.service.UsuarioService;
import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.dto.curso.DetalhamentoCurso;
import fly.be.flyflix.conteudo.dto.modulo.ModuloByListarPorCursoComOrdem;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CursoService {
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private ModuloService moduloService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private CursoModuloService cursoModuloService;

    public DetalhamentoCurso cadastrarCurso(CadastroCurso dados, Long userId) {
        Usuario autor = usuarioService.findByIdOrThrowsNotFoundException(userId);
        Curso curso = dados.toEntity(autor);
        Curso response = cursoRepository.save(curso);
        return DetalhamentoCurso.by(response);
    }

    @Transactional
    public void atualizarCurso(Long id, AtualizacaoCurso dados) {
        Curso curso = findByIdOrThrowsNotFoundException(id);
        curso.setTitulo(dados.titulo());
        curso.setDataPublicacao(dados.dataPublicacao());
        curso.setDataInicio(dados.dataInicio());
        curso.setDataConclusao(dados.dataConclusao());
        // Curso está em estado gerenciado, então o save() pode ser dispensado, mas chamar não faz mal:
        cursoRepository.save(curso);
    }

    @Transactional
    public DetalhamentoCurso adicionarModuloAoCurso(Long cursoId, Long moduloId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new NotFoundException("Curso não encontrado"));

        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(moduloId);

        Set<CursoModulo> cursoModulos = curso.getCursoModulos();

        boolean jaAssociado = cursoModulos.stream()
                .anyMatch(cm -> cm.getModulo().getId().equals(moduloId));
        if (jaAssociado) {
            throw new BadRequestException("Módulo já está associado a este curso");
        }

        int novaOrdem = cursoModulos.stream()
                .mapToInt(CursoModulo::getOrdem)
                .max()
                .orElse(0) + 1;

        CursoModulo cursoModulo = new CursoModulo(curso, modulo, novaOrdem);
        cursoModulos.add(cursoModulo);

        // Atualize os totais do curso, se tiver esse método
        curso.atualizarTotais();

        // Salva o curso (e pelo cascade, salva o CursoModulo)
        cursoRepository.save(curso);

        return DetalhamentoCurso.by(curso);
    }



    public Curso findByIdOrThrowsNotFoundException(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Curso com id '%s' não encontrado".formatted(id)));
    }
    public void ajustarOrdemModulos(Curso curso, CursoModulo cursoModuloAlterado, int novaOrdem) {
        int ordemAtual = cursoModuloAlterado.getOrdem();

        if (novaOrdem < ordemAtual) {
            // Mover módulo para posição anterior — aumentar ordem dos módulos entre novaOrdem e ordemAtual -1
            curso.getCursoModulos().stream()
                    .filter(cm -> cm != cursoModuloAlterado)
                    .filter(cm -> cm.getOrdem() >= novaOrdem && cm.getOrdem() < ordemAtual)
                    .forEach(cm -> {
                        cm.setOrdem(cm.getOrdem() + 1);
                    });
        } else {
            // Mover módulo para posição posterior — diminuir ordem dos módulos entre ordemAtual +1 e novaOrdem
            curso.getCursoModulos().stream()
                    .filter(cm -> cm != cursoModuloAlterado)
                    .filter(cm -> cm.getOrdem() > ordemAtual && cm.getOrdem() <= novaOrdem)
                    .forEach(cm -> {
                        cm.setOrdem(cm.getOrdem() - 1);
                    });
        }

        cursoModuloAlterado.setOrdem(novaOrdem);
    }

    private void ajustarOrdemModulosParaInsercao(Curso curso, int novaOrdem) {
        curso.getCursoModulos().stream()
                .filter(cm -> cm.getOrdem() >= novaOrdem)
                .forEach(cm -> {
                    cm.setOrdem(cm.getOrdem() + 1);
                });
    }
    @Transactional
    public void adicionarOuAtualizarModuloNoCurso(Long cursoId, Long moduloId, Integer ordem) {
        Curso curso = findByIdOrThrowsNotFoundException(cursoId);
        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(moduloId);

        Optional<CursoModulo> cursoModuloOpt = curso.getCursoModulos().stream()
                .filter(cm -> cm.getModulo().getId().equals(moduloId))
                .findFirst();

        if (cursoModuloOpt.isPresent()) {
            CursoModulo existente = cursoModuloOpt.get();
            if (ordem != null && !ordem.equals(existente.getOrdem())) {
                ajustarOrdemModulos(curso, existente, ordem);
            }
            // senão, nada a fazer (já está na posição correta)
        } else {
            if (ordem == null) {
                ordem = curso.getCursoModulos().stream()
                        .mapToInt(CursoModulo::getOrdem)
                        .max()
                        .orElse(0) + 1;
            }
            ajustarOrdemModulosParaInsercao(curso, ordem);
            CursoModulo novo = new CursoModulo(curso, modulo, ordem);
            curso.getCursoModulos().add(novo);
        }
    }


    @Transactional(readOnly = true)
    public Page<DetalhamentoCurso> listar(Pageable paginacao) {
        Page<Curso> cursos = cursoRepository.findAll(paginacao);
        return cursos.map(DetalhamentoCurso::by);
    }

    @Transactional(readOnly = true)
    public DetalhamentoCurso detalhar(Long id) {
        Curso curso = findByIdOrThrowsNotFoundException(id);
        return DetalhamentoCurso.by(curso);
    }

    public void remover(Long id) {
        cursoRepository.delete(findByIdOrThrowsNotFoundException(id));
    }

//    public List<ModuloByListarPorCurso> listarModulosPorCurso(Long id) {
//        Curso curso = findByIdOrThrowsNotFoundException(id);
//
//        List<Modulo> modulos = moduloService.listarPorCurso(curso);
//
//        return modulos.stream()
//                .map(ModuloByListarPorCurso::by)
//                .toList();
//    }

    public List<ModuloByListarPorCursoComOrdem> listarModulosPorCursoComOrdem(Long id) {
        Curso curso = findByIdOrThrowsNotFoundException(id);
        return curso.getCursoModulos().stream()
                .sorted(java.util.Comparator.comparingInt(CursoModulo::getOrdem))
                .map(cm -> ModuloByListarPorCursoComOrdem.by(cm.getModulo(), cm.getOrdem()))
                .toList();
    }

    @Transactional
    public void removerModulo(Long idCurso, Long idModulo) {
        Curso curso = findByIdOrThrowsNotFoundException(idCurso);
        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(idModulo);

        CursoModulo cursoModulo = cursoModuloService.findByCursoAndModuloOrThrowsNotFoundException(curso, modulo);

        curso.getCursoModulos().remove(cursoModulo); // Remove da coleção
        cursoModuloService.remover(cursoModulo);     // Remove da base

        curso.atualizarTotais();                     // Recalcula as horas
        cursoRepository.save(curso);                 // Persiste atualização
    }

}
