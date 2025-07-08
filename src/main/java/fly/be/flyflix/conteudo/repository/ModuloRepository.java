package fly.be.flyflix.conteudo.repository;

import fly.be.flyflix.conteudo.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    List<Modulo> findByTituloContainingIgnoreCase(String titulo);
    @Query(value = "SELECT m.* FROM modulos m " +
            "JOIN curso_modulo cm ON m.id = cm.modulo_id " +
            "WHERE cm.curso_id = :cursoId ORDER BY cm.ordem", nativeQuery = true)
    List<Modulo> findByCursoId(@Param("cursoId") Long cursoId);
}

