package fly.be.flyflix.conteudo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "curso_modulo")
public class CursoModulo {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private CursoModuloId id = new CursoModuloId();

    @ManyToOne
    @MapsId("cursoId")
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne
    @MapsId("moduloId")
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(nullable = false)
    private Integer ordem;

    public CursoModulo(Curso curso, Modulo modulo, Integer ordem) {
        this.curso = curso;
        this.modulo = modulo;
        this.ordem = ordem;
        this.id = new CursoModuloId(
                curso != null ? curso.getId() : null,
                modulo != null ? modulo.getId() : null
        );
    }
}
