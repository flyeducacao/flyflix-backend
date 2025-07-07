package fly.be.flyflix.conteudo.repository;

import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.CursoModuloId;
import fly.be.flyflix.conteudo.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CursoModuloRepository extends JpaRepository<CursoModulo, CursoModuloId> {
    Optional<CursoModulo> findByCursoAndModulo(Curso curso, Modulo modulo);
}

