package fly.be.flyflix.auth.controller.dto.aluno;

import fly.be.flyflix.auth.entity.Aluno;

public record AlunoResumoDTO(Long id, String nome) {
    public AlunoResumoDTO(Aluno aluno) {
        this(aluno.getId(), aluno.getNome());
    }
}


