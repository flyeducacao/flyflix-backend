package fly.be.flyflix.conteudo.entity;

import fly.be.flyflix.auth.entity.Aluno;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProgressoAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    private boolean assistida;

    public ProgressoAluno(Aluno aluno, Aula aula, Curso curso, boolean assistida) {
        this.aluno = aluno;
        this.aula = aula;
        this.curso = curso;
        this.assistida = assistida;
    }
}


