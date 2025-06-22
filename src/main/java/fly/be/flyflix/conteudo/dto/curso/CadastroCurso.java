package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.conteudo.dto.modulo.CadastroModulo;
import java.util.List;

import java.util.List;

public record CadastroCurso(
        String titulo,
        //String descricao,
        //String imagemCapa,
        List<Long> modulosIds,
        Long autorId  // novo campo para identificar o autor do curso
) {}




