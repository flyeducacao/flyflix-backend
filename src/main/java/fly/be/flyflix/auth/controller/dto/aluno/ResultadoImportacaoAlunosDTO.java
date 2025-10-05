package fly.be.flyflix.auth.controller.dto.aluno;
import java.util.List;



// Resultado de alunos importados (opcional)
public record ResultadoImportacaoAlunosDTO(
        List<MatriculaEmLoteResponse> resultados
) {}