package fly.be.flyflix.conteudo.dto.aula;

import fly.be.flyflix.conteudo.entity.Aula;

public record DadosDetalhamentoAula(
        Long id,
        String titulo,
        String tipo,
        Integer ordem,
        Integer duracaoEstimada,
        String linkConteudo,
        Long moduloId,        // Agora pode ser null
        String urlCapa        // Pode ser URL pública ou baseada em base64, conforme o uso
) {
    public DadosDetalhamentoAula(Aula aula) {
        this(
                aula.getId(),
                aula.getTitulo(),
                aula.getTipo(),
                aula.getOrdem(),
                aula.getDuracaoEstimada(),
                aula.getLinkConteudo(),
                aula.getModulo() != null ? aula.getModulo().getId() : null,
                aula.getCapa() != null ? "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(aula.getCapa()) : null
        );
    }
}



