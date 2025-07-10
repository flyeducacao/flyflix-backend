package fly.be.flyflix.conteudo.dto.modulo;

import fly.be.flyflix.conteudo.entity.Modulo;

public record ModuloByListarPorCursoComOrdem(Long id, String titulo, Integer ordem) {
    public static ModuloByListarPorCursoComOrdem by(Modulo modulo, Integer ordem) {
        return new ModuloByListarPorCursoComOrdem(modulo.getId(), modulo.getTitulo(), ordem);
    }
}
