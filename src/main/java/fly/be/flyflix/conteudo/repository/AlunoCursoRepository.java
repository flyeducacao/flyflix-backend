package fly.be.flyflix.conteudo.repository;

import fly.be.flyflix.auth.entity.AlunoCurso;
import fly.be.flyflix.conteudo.entity.AlunoCursoKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoCursoRepository extends JpaRepository<AlunoCurso, AlunoCursoKey> {
}
