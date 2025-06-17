package fly.be.flyflix.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fly.be.flyflix.auth.enums.PerfilAluno;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.conteudo.entity.Curso;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Aluno extends Usuario {

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_aluno", nullable = false)
    private PerfilAluno perfilAluno;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToMany
    @JsonIgnoreProperties("alunos") // evita loop de serialização
    @JsonIgnore
    @JoinTable(
            name = "aluno_curso",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
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

    @PrePersist
    public void prePersist() {
        super.setRole(Role.ALUNO);
    }
}

