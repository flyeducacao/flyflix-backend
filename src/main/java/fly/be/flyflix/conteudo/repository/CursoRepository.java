package fly.be.flyflix.conteudo.repository;

import fly.be.flyflix.conteudo.entity.Curso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    @EntityGraph(attributePaths = {"cursoModulos", "cursoModulos.modulo", "cursoModulos.modulo.aulas"})
    Optional<Curso> findById(Long id);
    @EntityGraph(attributePaths = {"cursoModulos", "cursoModulos.modulo", "cursoModulos.modulo.aulas"})
    Optional<Curso> findWithModulosAndAulasById(Long id);

    List<Curso> findTop10ByOrderByDataPublicacaoDesc();

}


