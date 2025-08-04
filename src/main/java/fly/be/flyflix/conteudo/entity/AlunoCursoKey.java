package fly.be.flyflix.conteudo.entity;

import fly.be.flyflix.auth.entity.Aluno;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class AlunoCursoKey implements Serializable {
    @Column(name = "aluno_id")
    private Long alunoId;
    @Column(name = "curso_id")
    private Long cursoId;

    public static AlunoCursoKey by(Aluno aluno, Curso curso) {
        return AlunoCursoKey.builder().alunoId(aluno.getId()).cursoId(curso.getId()).build();
    }
}