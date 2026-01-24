package fly.be.flyflix.conteudo.dto.modulo;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizacaoModulo(
        @NotNull(message = "O campo 'id' é obrigatório'")
        Long id,
        @NotBlank(message = "O campo 'titulo' é obrigatório'")
        String titulo
) {}