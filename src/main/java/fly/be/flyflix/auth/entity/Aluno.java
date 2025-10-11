package fly.be.flyflix.auth.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.ProgressoAluno;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Aluno extends Usuario {

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // evita loops caso serialize
    @Builder.Default
    private List<ProgressoAluno> progresso = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "aluno_curso",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    @Builder.Default
    private Set<Curso> cursos = new HashSet<>();

    public boolean inativar() {
        if (Boolean.TRUE.equals(this.ativo)) {
            this.ativo = false;
            return true;
        }
        return false;
    }

    public boolean ativar() {
        if (Boolean.FALSE.equals(this.ativo)) {
            this.ativo = true;
            return true;
        }
        return false;
    }
}
