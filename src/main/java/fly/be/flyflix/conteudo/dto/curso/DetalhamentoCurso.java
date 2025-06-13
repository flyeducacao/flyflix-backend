package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.conteudo.dto.modulo.DetalhamentoModulo;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;

import java.util.Comparator;
import java.util.List;

public record DetalhamentoCurso(
        Long id,
        String titulo,
        String descricao,
        String imagemCapa,
        List<DetalhamentoModulo> modulos
) {
    // Para detalhes com módulos (GET /api/cursos/{id})
    public DetalhamentoCurso(Curso curso) {
        this(
                curso.getId(),
                curso.getTitulo(),
                curso.getDescricao(),
                curso.getImagemCapa(),
                curso.getCursoModulos().stream()
                        .sorted(Comparator.comparing(CursoModulo::getOrdem))
                        .map(cm -> new DetalhamentoModulo(cm.getModulo()))
                        .toList()
        );
    }

    // Para listagem sem módulos (GET /api/cursos)
    public static DetalhamentoCurso semModulos(Curso curso) {
        return new DetalhamentoCurso(
                curso.getId(),
                curso.getTitulo(),
                curso.getDescricao(),
                curso.getImagemCapa(),
                null // ou List.of() para lista vazia
        );
    }
}



