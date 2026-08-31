
package fly.be.flyflix.auth.controller.dto.aluno;

import java.time.LocalDate;

public record ErroImportacaoAlunoDTO(
        int linha,
        String nome,
        String email,
        String cpf,
        LocalDate dataNascimento,
        String motivoErro
) {
    public ErroImportacaoAlunoDTO(CadastroAluno dto, int linha, String motivoErro) {
        this(linha, dto.nome(), dto.email(), dto.cpf(), dto.dataNascimento(), motivoErro);
    }
}

