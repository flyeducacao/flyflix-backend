package fly.be.flyflix.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O campo 'email' é obrigatório'")
        String email,
        @NotBlank(message = "O campo 'senha' é obrigatório'")
        String senha
) {
}




