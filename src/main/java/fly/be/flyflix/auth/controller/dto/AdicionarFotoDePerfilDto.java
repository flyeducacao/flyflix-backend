package fly.be.flyflix.auth.controller.dto;

import jakarta.validation.constraints.NotNull;

public record AdicionarFotoDePerfilDto(@NotNull(message = "O campo 'url' é obrigatório") String url) {
}
