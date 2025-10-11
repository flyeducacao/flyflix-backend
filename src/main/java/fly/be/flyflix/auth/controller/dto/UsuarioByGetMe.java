package fly.be.flyflix.auth.controller.dto;

import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.entity.Usuario;
import java.util.List;

/**
 * DTO usado para retornar os dados do usuário autenticado no endpoint /usuarios/me.
 */
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioByGetMe(
        Long id,
        String nome,
        String email,
        String fotoPerfilUrl,
        List<Long> cursoIds
) {
    public static UsuarioByGetMe by(Usuario usuario) {
        List<Long> cursoIds = null;

        if (usuario instanceof Aluno aluno) {
            cursoIds = aluno.getCursos().stream()
                    .map(alunoCurso -> alunoCurso.getCurso().getId())
                    .toList(); // retorna lista vazia se não tiver cursos
        }

        return new UsuarioByGetMe(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getFotoPerfilUrl(),
                cursoIds
        );
    }
}