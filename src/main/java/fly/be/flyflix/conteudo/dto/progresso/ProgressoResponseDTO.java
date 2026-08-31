package fly.be.flyflix.conteudo.dto.progresso;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProgressoResponseDTO {
    private Long alunoId;
    private Long cursoId;
    private Long totalAulas;
    private Long aulasAssistidas;
    private double porcentagem;

    public ProgressoResponseDTO(Long alunoId, Long cursoId, Long totalAulas, Long aulasAssistidas, double porcentagem) {
        this.alunoId = alunoId;
        this.cursoId = cursoId;
        this.totalAulas = totalAulas;
        this.aulasAssistidas = aulasAssistidas;
        this.porcentagem = porcentagem;
    }
}

