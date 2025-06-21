package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.conteudo.entity.Curso;

import java.time.LocalDate;

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
        UsuarioByDetalhamentoCurso autor
) {
    // Para detalhes com módulos (GET /api/cursos/{id})
    public static DetalhamentoCurso by(Curso curso) {
        UsuarioByDetalhamentoCurso autor = new UsuarioByDetalhamentoCurso(curso.getAutor());

        return new DetalhamentoCurso(
                curso.getId(),
                curso.getTitulo(),
                curso.getDescricao(),
                curso.getImagemCapa(),
                curso.getDataPublicacao(),
                autor
        );
    }
}



