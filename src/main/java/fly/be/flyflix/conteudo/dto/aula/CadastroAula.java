package fly.be.flyflix.conteudo.dto.aula;

public record CadastroAula(
        String titulo,
        String tipo,
        Integer ordem,
        Integer duracaoEstimada,
        String linkConteudo,
        Long moduloId
) {}