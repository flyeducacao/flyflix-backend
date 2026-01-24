package fly.be.flyflix.auth.controller.dto.aluno;

import fly.be.flyflix.conteudo.dto.curso.CursoResumoDTO;

import java.util.List;

public record MatriculaResponseDTO(
        Long alunoId,
        String nomeAluno,
        List<CursoResumoDTO> cursosMatriculados
) {}
