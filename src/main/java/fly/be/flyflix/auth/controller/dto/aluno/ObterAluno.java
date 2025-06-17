package fly.be.flyflix.auth.controller.dto.aluno;

import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.enums.PerfilAluno;

import java.time.LocalDate;

public record ObterAluno(
        Long id,
        String cpf,
        String nome,
        String email,
        LocalDate dataNascimento,
        Boolean ativo,
        PerfilAluno perfilAluno) {

    public ObterAluno(Aluno aluno) {
        this(aluno.getId(), aluno.getCpf(), aluno.getNome(), aluno.getEmail(), aluno.getDataNascimento(), aluno.getAtivo(), aluno.getPerfilAluno());
    }
}
