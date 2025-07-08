package fly.be.flyflix.auth.controller.dto.senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record RequisicaoResetSenhaDTO(@NotBlank @Email String email) {

}


