package fly.be.flyflix.auth.repository;

import fly.be.flyflix.auth.entity.AlunoCurso;
import fly.be.flyflix.auth.entity.AlunoCursoKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlunoCursoRepository extends JpaRepository<AlunoCurso, AlunoCursoKey> {
}

