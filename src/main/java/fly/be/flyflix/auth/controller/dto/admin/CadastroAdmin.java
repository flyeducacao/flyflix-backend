
package fly.be.flyflix.auth.controller.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CadastroAdmin(
        @NotBlank(message = "O campo 'nome' é obrigatório'")
        String nome,
        @NotBlank(message = "O campo 'email' é obrigatório'")
        @Email(message = "Email inválido")
        String email,
        @NotBlank(message = "O campo 'cpf' é obrigatório'")
        String cpf
) {}
