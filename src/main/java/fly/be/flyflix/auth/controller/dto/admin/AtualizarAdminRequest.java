package fly.be.flyflix.auth.controller.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarAdminRequest(
        @NotNull Long id,
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank String cpf
) {}
