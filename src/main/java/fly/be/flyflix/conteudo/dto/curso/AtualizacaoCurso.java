package fly.be.flyflix.conteudo.dto.curso;

public record AtualizacaoCurso(
        String titulo,
        //String descricao,
        //String imagemCapa,
        Long autorId  // permite atualizar o autor do curso
) {}
