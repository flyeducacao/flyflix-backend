package fly.be.flyflix.auth.controller.dto.aluno;

import java.util.Set;

public record MatriculaEmLoteRequest(
        Set<Long> alunoIds,
        Long cursoId
) {}
