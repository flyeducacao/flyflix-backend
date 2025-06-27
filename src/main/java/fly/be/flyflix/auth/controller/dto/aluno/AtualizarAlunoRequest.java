package fly.be.flyflix.auth.controller.dto.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record AtualizarAlunoRequest(
        @NotNull(message = "Id é obrigatório")
        Long id,
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "O email deve ser válido")
        String email,
        @NotBlank String cpf,
        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento inválida")
        LocalDate dataNascimento
) {}
