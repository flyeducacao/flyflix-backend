package fly.be.flyflix.conteudo.repository;

import fly.be.flyflix.conteudo.dto.aula.AulaResumoDTO;
import fly.be.flyflix.conteudo.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {
    List<Aula> findByModuloId(Long moduloId);
    @Query("""
    SELECT COUNT(a)
    FROM Aula a
    JOIN a.modulo m
    JOIN CursoModulo cm ON cm.modulo = m
    WHERE cm.curso.id = :cursoId
""")
    long countAulasByCursoId(@Param("cursoId") Long cursoId);
    @Query("""
    SELECT new fly.be.flyflix.conteudo.dto.aula.AulaResumoDTO(a.id, a.titulo, a.tipo, a.ordem)
    FROM Aula a
    JOIN a.modulo m
    JOIN CursoModulo cm ON cm.modulo = m
    WHERE cm.curso.id = :cursoId
""")
    List<AulaResumoDTO> findAulasResumoByCursoId(@Param("cursoId") Long cursoId);

}
