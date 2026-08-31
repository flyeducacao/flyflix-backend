
package fly.be.flyflix.auth.controller.dto.senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AtualizarSenhaDTO(
        @NotBlank(message = "O campo 'email' é obrigatório")
        @Email(message = "Email inválido")
        String email,
        @NotBlank(message = "O campo 'senhaAtual' é obrigatório")
        String senhaAtual,
        @NotBlank(message = "O campo 'novaSenha' é obrigatório")
        String novaSenha
) {}
