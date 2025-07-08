
package fly.be.flyflix.conteudo.dto.progresso;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class ProgressoRequestDTO {
    private Long alunoId;
    private Long aulaId;
    private Long cursoId;

}

