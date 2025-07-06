package fly.be.flyflix.auth.controller.dto;

import fly.be.flyflix.auth.entity.Usuario;

public record UsuarioByGetMe(Long id, String username, String email, String urlFoto) {
    public static UsuarioByGetMe by(Usuario usuario, String urlFoto) {
        return new UsuarioByGetMe(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                urlFoto
        );
    }
}
