package fly.be.flyflix.auth.controller.dto.senha;

import jakarta.validation.constraints.NotBlank;

public record RedefinicaoSenhaDTO(
        @NotBlank(message = "O campo 'token' é obrigatório'")
        String token,
        @NotBlank(message = "O campo 'novaSenha' é obrigatório'")
        String novaSenha
) {}


