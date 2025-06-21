package fly.be.flyflix.conteudo.dto.modulo;

import fly.be.flyflix.conteudo.entity.Modulo;

public record ModuloByListarPorCurso(Long id, String titulo) {
    public static ModuloByListarPorCurso by(Modulo modulo) {
        return new ModuloByListarPorCurso(modulo.getId(), modulo.getTitulo());
    }
}
