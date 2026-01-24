package fly.be.flyflix.conteudo.dto.modulo;

import fly.be.flyflix.conteudo.dto.aula.AulaResumoDTO;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;

import fly.be.flyflix.conteudo.entity.Aula;
import fly.be.flyflix.conteudo.entity.Modulo;

import java.util.Comparator;
import java.util.List;

import fly.be.flyflix.conteudo.entity.Aula;
import fly.be.flyflix.conteudo.entity.Modulo;

import java.util.List;

public record DetalhamentoModulo(
        Long id,
        String titulo,
        List<AulaResumoDTO> aulas
) {
    public DetalhamentoModulo(Modulo modulo) {
        this(
                modulo.getId(),
                modulo.getTitulo(),
                modulo.getAulas().stream()
                        .sorted(Comparator.comparingInt(a -> a.getOrdem() != null ? a.getOrdem() : 0))
                        .map(AulaResumoDTO::new)
                        .toList()
        );
    }
}



