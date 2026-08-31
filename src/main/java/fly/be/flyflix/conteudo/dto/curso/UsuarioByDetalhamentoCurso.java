package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.auth.entity.Usuario;

public record UsuarioByDetalhamentoCurso(Long id, String nome, String email) {
    public UsuarioByDetalhamentoCurso(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}
