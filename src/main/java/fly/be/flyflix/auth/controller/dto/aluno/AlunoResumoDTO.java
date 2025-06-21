package fly.be.flyflix.auth.controller.dto.aluno;

import fly.be.flyflix.auth.entity.Aluno;

public record AlunoResumoDTO(Long id, String nome, String email, Boolean ativo) {
    public AlunoResumoDTO(Aluno aluno) {
        this(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getAtivo());
    }
}


