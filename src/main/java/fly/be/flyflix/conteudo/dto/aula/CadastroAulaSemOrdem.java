package fly.be.flyflix.conteudo.dto.aula;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastroAulaSemOrdem(
        @NotBlank(message = "O campo 'titulo' é obrigatório")
        String titulo,
        @NotBlank(message = "O campo 'tipo' é obrigatório")
        String tipo,
        @NotNull(message = "O campo 'duracaoEstimada' é obrigatório")
        Integer duracaoEstimada,
        @NotBlank(message = "O campo 'linkConteudo' é obrigatório")
        String linkConteudo,
        @NotNull(message = "O campo 'moduloId' é obrigatório")
        Long moduloId
) {}