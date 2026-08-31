package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.conteudo.entity.Curso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CadastroCurso(
        @NotBlank(message = "O campo 'titulo' é obrigatório")
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
) {
        public Curso toEntity(Usuario autor) {
                return Curso.builder()
                        .titulo(this.titulo())
                        .dataPublicacao(this.dataPublicacao() != null ? this.dataPublicacao() : LocalDate.now())
                        .dataInicio(this.dataInicio())
                        .dataConclusao(this.dataConclusao())
                        .autor(autor)
                        .build();

        }
}






