package fly.be.flyflix.conteudo.repository;

import fly.be.flyflix.conteudo.entity.Modulo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    @EntityGraph(attributePaths = "aulas")
    @Override
    List<Modulo> findAll();

    @EntityGraph(attributePaths = "aulas")
    Optional<Modulo> findById(Long id);

    List<Modulo> findByTituloContainingIgnoreCase(String titulo);

    @Query("""
        SELECT cm.modulo FROM CursoModulo cm
        WHERE cm.curso.id = :cursoId
        ORDER BY cm.ordem
    """)
    List<Modulo> findByCursoId(@Param("cursoId") Long cursoId);

    @EntityGraph(attributePaths = {"aulas"})
    @Query("""
        SELECT cm.modulo FROM CursoModulo cm
        WHERE cm.curso.id = :cursoId
        ORDER BY cm.ordem
    """)
    List<Modulo> findByCursoIdComAulas(@Param("cursoId") Long cursoId);

    boolean existsByTituloIgnoreCase(String titulo);
}




