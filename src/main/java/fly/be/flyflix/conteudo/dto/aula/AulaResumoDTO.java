package fly.be.flyflix.conteudo.dto.aula;

import fly.be.flyflix.conteudo.entity.Aula;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AulaResumoDTO {
    private Long id;
    private String titulo;
    private String tipo;
    private Integer ordem;

    public AulaResumoDTO(Aula aula) {
        this.id = aula.getId();
        this.titulo = aula.getTitulo();
        this.tipo = aula.getTipo();
        this.ordem = aula.getOrdem();
    }
}
