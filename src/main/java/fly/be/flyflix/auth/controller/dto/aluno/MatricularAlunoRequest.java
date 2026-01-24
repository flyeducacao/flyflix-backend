package fly.be.flyflix.auth.controller.dto.aluno;

import java.util.Set;

public record MatricularAlunoRequest(Long alunoId, Set<Long> cursoIds) {
}





