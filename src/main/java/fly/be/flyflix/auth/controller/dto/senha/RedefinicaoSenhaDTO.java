package fly.be.flyflix.auth.controller.dto.senha;

import jakarta.validation.constraints.NotBlank;

public record RedefinicaoSenhaDTO(
        @NotBlank String token,
        @NotBlank String novaSenha
) {}


