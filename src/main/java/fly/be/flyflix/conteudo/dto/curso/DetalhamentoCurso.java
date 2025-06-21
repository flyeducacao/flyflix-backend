package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.conteudo.dto.modulo.DetalhamentoModulo;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

record UsuarioByDetalhamentoCurso(Long id, String nome, String email) {
    public UsuarioByDetalhamentoCurso(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}

public record DetalhamentoCurso(
        Long id,
        String titulo,
        String descricao,
        String imagemCapa,
        LocalDate dataPublicacao,
        UsuarioByDetalhamentoCurso autor,
        List<DetalhamentoModulo> modulos
) {
    // Para detalhes com módulos (GET /api/cursos/{id})
    public static DetalhamentoCurso by(Curso curso) {
        UsuarioByDetalhamentoCurso autor = new UsuarioByDetalhamentoCurso(curso.getAutor());

        Set<CursoModulo> cursoModuloSet = curso.getCursoModulos();

        List<DetalhamentoModulo> detalhamentoModuloList = cursoModuloSet.stream()
                .sorted(Comparator.comparing(CursoModulo::getOrdem))
                .map(cm -> new DetalhamentoModulo(cm.getModulo()))
                .toList();

        return new DetalhamentoCurso(
                curso.getId(),
                curso.getTitulo(),
                curso.getDescricao(),
                curso.getImagemCapa(),
                curso.getDataPublicacao(),
                autor,
                detalhamentoModuloList
        );
    }
}



