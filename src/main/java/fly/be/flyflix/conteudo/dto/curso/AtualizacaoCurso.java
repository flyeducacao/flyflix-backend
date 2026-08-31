package fly.be.flyflix.conteudo.dto.curso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AtualizacaoCurso(
        @NotBlank(message = "O campo 'titulo' é obrigatório'")
        String titulo,
        @NotNull(message = "A data de publicação é obrigatória")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataPublicacao,
        @NotNull(message = "A data de início é obrigatória")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,
        @NotNull(message = "A data de conclusão é obrigatória")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataConclusao
) {}
