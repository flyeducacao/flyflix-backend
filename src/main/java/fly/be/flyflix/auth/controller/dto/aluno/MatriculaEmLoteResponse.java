package fly.be.flyflix.auth.controller.dto.aluno;

import java.util.List;

// Resposta de cada linha da importação/matrícula
public record MatriculaEmLoteResponse(
        String nome,
        String email,
        String status,
        String mensagemErro
) {
    public MatriculaEmLoteResponse(String nome, String email, String status) {
        this(nome, email, status, null);
    }
}


