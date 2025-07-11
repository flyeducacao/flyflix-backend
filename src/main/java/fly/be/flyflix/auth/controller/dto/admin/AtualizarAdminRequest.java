package fly.be.flyflix.auth.controller.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarAdminRequest(
        @NotNull(message = "O campo 'id' é obrigatório'")
        Long id,
        @NotBlank(message = "O campo 'nome' é obrigatório'")
        String nome,
        @Email(message = "Email inválido")
        @NotBlank(message = "O campo 'email' é obrigatório'")
        String email,
        @NotBlank(message = "O campo 'cpf' é obrigatório'")
        String cpf
) {}
