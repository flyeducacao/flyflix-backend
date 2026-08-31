package fly.be.flyflix.auth.controller.dto.aluno;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record MatriculaEmLoteRequest(
        @NotNull(message = "O campo 'alunoIds' é obrigatório")
        Set<Long> alunoIds,
        @NotNull(message = "O campo 'cursoId' é obrigatório")
        Long cursoId
) {}
