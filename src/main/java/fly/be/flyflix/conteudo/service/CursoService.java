package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModuloRepository moduloRepository;

    @Transactional
    public Curso cadastrarCurso(CadastroCurso dados) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.valueOf(authentication.getName());

            Usuario autor = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado"));

            Curso curso = Curso.builder()
                    .titulo(dados.titulo())
                    //.descricao(dados.descricao())
                    //.imagemCapa(dados.imagemCapa())
                    .dataPublicacao(LocalDate.now())
                    .autor(autor)
                    .build();

            curso = cursoRepository.save(curso); // Salva primeiro para garantir ID e persistência

            if (dados.modulosIds() != null && !dados.modulosIds().isEmpty()) {
                List<Modulo> modulos = moduloRepository.findAllById(dados.modulosIds());

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
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));

        if (dados.titulo() != null) curso.setTitulo(dados.titulo());
        //if (dados.descricao() != null) curso.setDescricao(dados.descricao());
        //if (dados.imagemCapa() != null) curso.setImagemCapa(dados.imagemCapa());

        if (dados.autorId() != null) {
            Usuario novoAutor = usuarioRepository.findById(dados.autorId())
                    .orElseThrow(() -> new RuntimeException("Autor informado não encontrado"));
            curso.setAutor(novoAutor);
        }

        return cursoRepository.save(curso);
    }
    @Transactional
    public Curso adicionarModuloAoCurso(Long cursoId, Long moduloId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado"));

        Modulo modulo = moduloRepository.findById(moduloId)
                .orElseThrow(() -> new IllegalArgumentException("Módulo não encontrado"));

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

        // Relacionamento bidirecional
        curso.getCursoModulos().add(cursoModulo);
        modulo.getCursoModulos().add(cursoModulo);

        // Só precisa salvar o lado "owner" da relação (CursoModulo)
        cursoModuloRepository.save(cursoModulo);

        return curso;
    }

}
