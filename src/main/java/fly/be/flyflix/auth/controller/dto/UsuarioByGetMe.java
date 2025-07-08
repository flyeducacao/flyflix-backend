package fly.be.flyflix.auth.controller.dto;

import fly.be.flyflix.auth.entity.Usuario;

/**
 * DTO usado para retornar os dados do usuário autenticado no endpoint /usuarios/me.
 */
public record UsuarioByGetMe(
        Long id,
        String nome,
        String email,
        String fotoPerfilUrl
) {
    public static UsuarioByGetMe by(Usuario usuario, String fotoPerfilUrl) {
        return new UsuarioByGetMe(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                fotoPerfilUrl
        );
    }
}

