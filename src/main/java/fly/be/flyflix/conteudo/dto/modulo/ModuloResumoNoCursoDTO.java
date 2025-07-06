package fly.be.flyflix.conteudo.dto.modulo;


import fly.be.flyflix.conteudo.entity.CursoModulo;

public record ModuloResumoNoCursoDTO(
        Long id,
        String titulo,
        Integer ordem
) {
    public static ModuloResumoNoCursoDTO from(CursoModulo cursoModulo) {
        return new ModuloResumoNoCursoDTO(
                cursoModulo.getModulo().getId(),
                cursoModulo.getModulo().getTitulo(),
                cursoModulo.getOrdem()
        );
    }
}
