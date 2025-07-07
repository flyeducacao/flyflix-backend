package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.service.UsuarioService;
import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.dto.curso.DetalhamentoCurso;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.CursoModuloRepository;
import fly.be.flyflix.conteudo.repository.CursoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CursoService {
    private static final Logger log = LoggerFactory.getLogger(CursoService.class);

    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private CursoModuloRepository cursoModuloRepository;
    @Autowired
    private ModuloService moduloService;
    @Autowired
    private UsuarioService usuarioService;

    public DetalhamentoCurso cadastrarCurso(CadastroCurso dados, Long userId) {
        Usuario autor = usuarioService.findByIdOrThrowsNotFoundException(userId);

        Curso curso = Curso.builder()
                .titulo(dados.titulo())
                .dataPublicacao(LocalDate.now())
                .autor(autor)
                .build();

        Curso response = cursoRepository.save(curso);

        return DetalhamentoCurso.by(response);
    }

    public void atualizarCurso(Long id, AtualizacaoCurso dados) {
        Curso curso = findByIdOrThrowsNotFoundException(id);
        curso.setTitulo(dados.titulo());

        cursoRepository.save(curso);
    }
    @Transactional
    public Curso adicionarModuloAoCurso(Long cursoId, Long moduloId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new NotFoundException("Curso não encontrado"));

        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(moduloId);

        boolean jaAssociado = curso.getCursoModulos().stream()
                .anyMatch(cm -> cm.getModulo().getId().equals(moduloId));
        if (jaAssociado) {
            throw new BadRequestException("Módulo já está associado a este curso");
        }

        int novaOrdem = curso.getCursoModulos().stream()
                .mapToInt(CursoModulo::getOrdem)
                .max()
                .orElse(0) + 1;

        CursoModulo cursoModulo = new CursoModulo(curso, modulo, novaOrdem);
        cursoModuloRepository.save(cursoModulo);

        // curso.getAutor() e curso.getCursoModulos() estarão prontos para uso no DTO
        return curso;
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
                        cursoModuloRepository.save(cm);
                    });
        } else {
            // Mover módulo para posição posterior — diminuir ordem dos módulos entre ordemAtual +1 e novaOrdem
            curso.getCursoModulos().stream()
                    .filter(cm -> cm != cursoModuloAlterado)
                    .filter(cm -> cm.getOrdem() > ordemAtual && cm.getOrdem() <= novaOrdem)
                    .forEach(cm -> {
                        cm.setOrdem(cm.getOrdem() - 1);
                        cursoModuloRepository.save(cm);
                    });
        }

        cursoModuloAlterado.setOrdem(novaOrdem);
        cursoModuloRepository.save(cursoModuloAlterado);
    }

    private void ajustarOrdemModulosParaInsercao(Curso curso, int novaOrdem) {
        curso.getCursoModulos().stream()
                .filter(cm -> cm.getOrdem() >= novaOrdem)
                .forEach(cm -> {
                    cm.setOrdem(cm.getOrdem() + 1);
                    cursoModuloRepository.save(cm);
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
            cursoModuloRepository.save(novo);
        }
    }


    public Page<DetalhamentoCurso> listar(Pageable paginacao) {
        Page<Curso> cursos = cursoRepository.findAll(paginacao);

        return cursos.map(DetalhamentoCurso::by);
    }

    public DetalhamentoCurso detalhar(Long id) {
        Curso curso = findByIdOrThrowsNotFoundException(id);

        return DetalhamentoCurso.by(curso);
    }
}
