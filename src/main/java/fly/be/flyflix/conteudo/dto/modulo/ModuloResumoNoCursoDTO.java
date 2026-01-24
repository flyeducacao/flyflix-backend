package fly.be.flyflix.conteudo.dto.modulo;


import fly.be.flyflix.conteudo.entity.CursoModulo;

public record ModuloResumoNoCursoDTO(
        Long id,
        String titulo,
        Integer ordem
) {
    public static ModuloResumoNoCursoDTO from(CursoModulo cursoModulo) {
        if (cursoModulo == null || cursoModulo.getModulo() == null) {
            throw new IllegalArgumentException("CursoModulo ou Modulo está nulo");
        }

        return new ModuloResumoNoCursoDTO(
                cursoModulo.getModulo().getId(),
                cursoModulo.getModulo().getTitulo(),
                cursoModulo.getOrdem()
        );
    }
}

