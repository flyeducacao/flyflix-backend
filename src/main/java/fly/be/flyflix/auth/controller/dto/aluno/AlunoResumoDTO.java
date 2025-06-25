package fly.be.flyflix.auth.controller.dto.aluno;

import fly.be.flyflix.auth.entity.Aluno;

import java.time.LocalDate;

public record AlunoResumoDTO(Long id, String nome, String email, LocalDate dataNascimento, Boolean ativo) {
    public AlunoResumoDTO(Aluno aluno) {
        this(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getDataNascimento(),aluno.getAtivo());
    }
}


