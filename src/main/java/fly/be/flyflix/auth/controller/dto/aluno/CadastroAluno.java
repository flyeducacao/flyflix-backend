package fly.be.flyflix.auth.controller.dto.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record CadastroAluno(
        @NotBlank(message = "O campo 'nome' é obrigatório")
        String nome,

        @NotBlank(message = "O campo 'email' é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "O campo 'cpf' é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotNull(message = "O campo 'dataNascimento' é obrigatório")
        @Past(message = "Data de nascimento inválida")
        LocalDate dataNascimento
) {}
