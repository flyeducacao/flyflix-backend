package fly.be.flyflix.auth.entity;

import fly.be.flyflix.conteudo.entity.AlunoCursoKey;
import fly.be.flyflix.conteudo.entity.Curso;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity(name = "aluno_curso")
@NoArgsConstructor
@AllArgsConstructor
public class AlunoCurso {
    @EmbeddedId
    private AlunoCursoKey id;
    @ManyToOne
    @MapsId("alunoId")
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;
    @ManyToOne
    @MapsId("cursoId")
    @JoinColumn(name = "curso_id")
    private Curso curso;
}