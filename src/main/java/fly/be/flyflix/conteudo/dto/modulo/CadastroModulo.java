package fly.be.flyflix.conteudo.dto.modulo;


import jakarta.validation.constraints.NotBlank;

public record CadastroModulo(
        @NotBlank(message = "O campo 'titulo' é obrigatório")
        String titulo
) {}
