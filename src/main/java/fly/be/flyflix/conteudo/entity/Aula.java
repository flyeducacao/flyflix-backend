package fly.be.flyflix.conteudo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "aulas")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Size(min = 3, max = 255)
    @Column(nullable = false)
    private String titulo;

    private String tipo; // vídeo, texto, artigo, quiz

    private Integer ordem;

    private Integer duracaoEstimada; // em minutos

    private String linkConteudo; // URL, pode ser link para vídeo ou outro tipo de conteúdo

    @Lob
    @Column(name = "capa")
    private byte[] capa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = true) // Permitir nulo para aulas desvinculadas
    private Modulo modulo;
}
