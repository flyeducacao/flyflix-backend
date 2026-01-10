
package fly.be.flyflix.conteudo.dto.progresso;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class ProgressoRequestDTO {
    @NotNull(message = "O campo 'alunoId' é obrigatório")
    private Long alunoId;
    @NotNull(message = "O campo 'aulaId' é obrigatório")
    private Long aulaId;
    @NotNull(message = "O campo 'cursoId' é obrigatório")
    private Long cursoId;

}

