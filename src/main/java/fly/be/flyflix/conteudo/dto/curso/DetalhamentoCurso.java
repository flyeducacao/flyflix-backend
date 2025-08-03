package fly.be.flyflix.conteudo.dto.curso;

import fly.be.flyflix.conteudo.dto.modulo.ModuloResumoNoCursoDTO;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


public record DetalhamentoCurso(
        Long id,
        String titulo,
        LocalDate dataPublicacao,
        LocalDate dataInicio,
        LocalDate dataConclusao,
        String duracaoFormatada,
        int totalAulas,
        double totalHoras,
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
                curso.getDataPublicacao(),
                curso.getDataInicio(),
                curso.getDataConclusao(),
                formatarDuracao(curso.getTotalHoras()),
                curso.getTotalAulas(),
                curso.getTotalHoras(),
                autor,
                modulos
        );
    }

    private static String formatarDuracao(double totalHoras) {
        int totalMinutos = (int) (totalHoras * 60);
        int horas = totalMinutos / 60;
        int minutos = totalMinutos % 60;
        return "%d horas e %d minutos".formatted(horas, minutos);
    }
}
