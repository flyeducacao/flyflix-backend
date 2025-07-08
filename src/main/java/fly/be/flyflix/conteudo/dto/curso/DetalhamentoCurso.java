package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.conteudo.dto.modulo.ModuloResumoNoCursoDTO;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

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
        //String descricao,
        //String imagemCapa,
        LocalDate dataPublicacao,
        UsuarioByDetalhamentoCurso autor,
        List<ModuloResumoNoCursoDTO> modulos
) {
    public static DetalhamentoCurso by(Curso curso) {
        UsuarioByDetalhamentoCurso autor = new UsuarioByDetalhamentoCurso(curso.getAutor());

        List<ModuloResumoNoCursoDTO> modulos = curso.getCursoModulos().stream()
                .sorted(Comparator.comparingInt(CursoModulo::getOrdem)) // ordena por ordem
                .map(ModuloResumoNoCursoDTO::from)
                .toList();

        return new DetalhamentoCurso(
                curso.getId(),
                curso.getTitulo(),
                //curso.getDescricao(),
                //curso.getImagemCapa(),
                curso.getDataPublicacao(),
                autor,
                modulos
        );
    }
}




