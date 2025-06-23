package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.service.UsuarioService;
import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

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

    @Transactional
    public Curso cadastrarCurso(CadastroCurso dados) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.valueOf(authentication.getName());

            Usuario autor = usuarioService.findByIdOrThrowsNotFoundException(userId);

            Curso curso = Curso.builder()
                    .titulo(dados.titulo())
                    //.descricao(dados.descricao())
                    //.imagemCapa(dados.imagemCapa())
                    .dataPublicacao(LocalDate.now())
                    .autor(autor)
                    .build();

            curso = cursoRepository.save(curso); // Salva primeiro para garantir ID e persistência

            if (dados.modulosIds() != null && !dados.modulosIds().isEmpty()) {
                List<Modulo> modulos = dados.modulosIds().stream().map((id) -> moduloService.findByIdOrThrowsNotFoundException(id)).toList();

                int ordem = 1;
                for (Modulo modulo : modulos) {
                    CursoModulo cursoModulo = new CursoModulo(curso, modulo, ordem++);
                    curso.getCursoModulos().add(cursoModulo);
                    modulo.getCursoModulos().add(cursoModulo);
                    cursoModuloRepository.save(cursoModulo);
                }
            }

            return curso;
        } catch (Exception e) {
            log.error("Erro ao cadastrar curso: {}", e.getMessage(), e);
            throw e;
        }
    }



    @Transactional
    public Curso atualizarCurso(Long cursoId, AtualizacaoCurso dados) {
        Curso curso = findByIdOrThrowsNotFoundException(cursoId);

        if (dados.titulo() != null) curso.setTitulo(dados.titulo());
        //if (dados.descricao() != null) curso.setDescricao(dados.descricao());
        //if (dados.imagemCapa() != null) curso.setImagemCapa(dados.imagemCapa());

        if (dados.autorId() != null) {
            Usuario novoAutor = usuarioService.findByIdOrThrowsNotFoundException(dados.autorId());
            curso.setAutor(novoAutor);
        }

        return cursoRepository.save(curso);
    }
    @Transactional
    public Curso adicionarModuloAoCurso(Long cursoId, Long moduloId) {
        Curso curso = findByIdOrThrowsNotFoundException(cursoId);
        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(moduloId);

        // Evita associação duplicada
        boolean jaAssociado = curso.getCursoModulos().stream()
                .anyMatch(cm -> cm.getModulo().getId().equals(moduloId));
        if (jaAssociado) {
            throw new IllegalStateException("Módulo já está associado a este curso");
        }

        // Define a próxima ordem disponível
        int novaOrdem = curso.getCursoModulos().stream()
                .mapToInt(CursoModulo::getOrdem)
                .max()
                .orElse(0) + 1;

        CursoModulo cursoModulo = new CursoModulo(curso, modulo, novaOrdem);
        cursoModuloRepository.save(cursoModulo);
        curso.getCursoModulos().forEach(cm -> cm.getModulo().getId()); // força load
        return curso;
    }

    public Curso findByIdOrThrowsNotFoundException(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Curso com id '%s' não encontrado".formatted(id)));
    }
}
