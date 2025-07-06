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

    // ✅ Busca curso com módulos e aulas carregadas
    @EntityGraph(attributePaths = {
            "cursoModulos",
            "cursoModulos.modulo",
            "cursoModulos.modulo.aulas"
    })
    Optional<Curso> findCursoWithModulosAndAulasById(Long id);

    // ✅ Busca curso com autor e módulos (sem aulas)
    @EntityGraph(attributePaths = {
            "autor",
            "cursoModulos",
            "cursoModulos.modulo"
    })
    Optional<Curso> findCursoWithAutorAndModulosById(Long id);

    // ✅ Busca os 10 cursos mais recentes (sem relacionamentos)
    List<Curso> findTop10ByOrderByDataPublicacaoDesc();
}



