package fly.be.flyflix.auth.controller.dto;

import fly.be.flyflix.auth.entity.Usuario;

public record UsuarioByGetMe(Long id, String username) {
    public static UsuarioByGetMe by(Usuario usuario) {
        return new UsuarioByGetMe(
                usuario.getId(),
                usuario.getNome()
        );
    }
}
