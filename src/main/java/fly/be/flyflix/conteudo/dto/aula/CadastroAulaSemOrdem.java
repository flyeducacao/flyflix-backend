package fly.be.flyflix.conteudo.dto.aula;

public record CadastroAulaSemOrdem(
        String titulo,
        String tipo,
        Integer duracaoEstimada,
        String linkConteudo,
        Long moduloId
) {}