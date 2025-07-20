package fly.be.flyflix.auth.controller.dto.aluno;
import java.util.List;

public record ResultadoImportacaoAlunosDTO(
        int totalImportados,
        List<ErroImportacaoAlunoDTO> erros
) {}
