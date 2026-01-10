package fly.be.flyflix.conteudo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "cursos", indexes = {
        @Index(name = "idx_curso_data_publicacao", columnList = "data_publicacao")
})
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @NotNull
    @Size(min = 3, max = 255)
    @Column(nullable = false)
    private String titulo;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "total_aulas")
    private int totalAulas;

    @Column(name = "total_horas")
    private double totalHoras;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<CursoModulo> cursoModulos = new HashSet<>();

    @ManyToMany(mappedBy = "cursos")
    @Builder.Default
    private Set<Aluno> alunos = new HashSet<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<ProgressoAluno> progresso = new ArrayList<>();

    /**
     * Atualiza os totais de aulas e duração do curso com base nas aulas dos módulos.
     */
    public void atualizarTotais() {
        int totalAulas = 0;
        int totalMinutos = 0;

        for (CursoModulo cursoModulo : cursoModulos) {
            Modulo modulo = cursoModulo.getModulo();
            if (modulo != null && modulo.getAulas() != null) {
                totalAulas += modulo.getAulas().size();
                totalMinutos += modulo.getAulas().stream()
                        .filter(aula -> aula.getDuracaoEstimada() != null)
                        .mapToInt(Aula::getDuracaoEstimada)
                        .sum();
            }
        }

        this.totalAulas = totalAulas;
        this.totalHoras = totalMinutos / 60.0;  // divisão com decimal
    }


    @PrePersist
    @PreUpdate
    public void onSaveOrUpdate() {
        atualizarTotais();
    }


}
