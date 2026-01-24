package fly.be.flyflix.conteudo.dto.aula;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoAula(
        @NotNull(message = "O campo 'id' é obrigatório'")
        Long id,
        @NotBlank(message = "O campo 'titulo' é obrigatório'")
        String titulo,
        @NotBlank(message = "O campo 'tipo' é obrigatório'")
        String tipo,
        @NotNull(message = "O campo 'ordem' é obrigatório'")
        Integer ordem,
        @NotNull(message = "O campo 'duracaoEstimada' é obrigatório'")
        Integer duracaoEstimada,
        @NotBlank(message = "O campo 'linkConteudo' é obrigatório'")
        String linkConteudo,
        @NotNull(message = "O campo 'moduloId' é obrigatório'")
        Long moduloId
) {}

