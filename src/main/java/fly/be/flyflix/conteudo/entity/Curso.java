package fly.be.flyflix.conteudo.entity;

import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

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

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    private String imagemCapa;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CursoModulo> cursoModulos = new ArrayList<>();

//    // Métodos de negócio usando CursoModulo
//    public void adicionarModulo(Modulo modulo, int ordem) {
//        CursoModulo cursoModulo = new CursoModulo(this, modulo, ordem);
//        cursoModulos.add(cursoModulo);
//        modulo.getCursoModulos().add(cursoModulo);
//    }
//
//    public void removerModulo(Modulo modulo) {
//        cursoModulos.removeIf(cm -> cm.getModulo().equals(modulo));
//        modulo.getCursoModulos().removeIf(cm -> cm.getCurso().equals(this));
//    }
    @ManyToMany(mappedBy = "cursos")
    private Set<Aluno> alunos = new HashSet<>();

}
