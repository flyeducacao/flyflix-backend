package fly.be.flyflix.conteudo.dto.curso;

import jakarta.validation.constraints.NotBlank;

public record AtualizacaoCurso(
        @NotBlank(message = "O campo 'titulo' é obrigatório'")
        String titulo
) {}
