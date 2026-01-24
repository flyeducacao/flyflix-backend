package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.conteudo.entity.Curso;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class CursoResumoDTO {
    private Long id;
    private String titulo;

}
