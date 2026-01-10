package fly.be.flyflix.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record AlteraFotoPerfilDto(
        @NotBlank(message = "O campo 'url' é obrigatório")
        String url
) {
}
