package fly.be.flyflix.conteudo.dto.aula;

import fly.be.flyflix.conteudo.entity.Aula;

public record AulasByListarModulos(
        Long id,
        String titulo,
        String tipo,
        Integer ordem
) {
    public static AulasByListarModulos by(Aula aula) {
        return new AulasByListarModulos(
                aula.getId(),
                aula.getTitulo(),
                aula.getTipo(),
                aula.getOrdem()
        );
    }
}
