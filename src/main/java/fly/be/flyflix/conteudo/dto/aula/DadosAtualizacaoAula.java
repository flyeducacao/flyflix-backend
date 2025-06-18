package fly.be.flyflix.conteudo.dto.aula;

public record DadosAtualizacaoAula(
        Long id,
        String titulo,
        String tipo,
        Integer ordem,
        Integer duracaoEstimada,
        String linkConteudo,
        Long moduloId
) {}

