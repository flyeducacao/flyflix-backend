package fly.be.flyflix.auth.controller.dto.aluno;

import fly.be.flyflix.auth.entity.Aluno;

import java.time.LocalDate;

public record AlunoResumoDTO(Long id, String nome, String cpf, String email, LocalDate dataNascimento, String urlFoto) {
    public AlunoResumoDTO(Aluno aluno, String urlFoto) {
        this(
                aluno.getId(),
                aluno.getNome(),
                aluno.getCpf(),
                aluno.getEmail(),
                aluno.getDataNascimento(),
                urlFoto
        );
    }
}


