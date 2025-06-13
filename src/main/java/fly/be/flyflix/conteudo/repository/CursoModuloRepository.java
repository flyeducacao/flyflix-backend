package fly.be.flyflix.conteudo.repository;

import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.CursoModuloId;
import fly.be.flyflix.conteudo.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;

@Repository
public interface CursoModuloRepository extends JpaRepository<CursoModulo, CursoModuloId> {

    // Busca todos os CursoModulo de um curso, ordenados pela ordem
    List<CursoModulo> findByCurso_IdOrderByOrdemAsc(Long cursoId);
    Optional<CursoModulo> findByCursoAndModulo(Curso curso, Modulo modulo);



    // Busca apenas os módulos de um curso (via CursoModulo), ordenados
    @Query("SELECT cm.modulo FROM CursoModulo cm WHERE cm.curso.id = :cursoId ORDER BY cm.ordem")
    List<Modulo> findModulosByCursoId(@Param("cursoId") Long cursoId);

    // Verifica se já existe a associação entre um curso e um módulo
    boolean existsByCursoAndModulo(Curso curso, Modulo modulo);

    // Retorna a maior ordem atual dos módulos do curso (útil para adicionar um novo no final)
    @Query("SELECT MAX(cm.ordem) FROM CursoModulo cm WHERE cm.curso = :curso")
    Optional<Integer> findMaxOrdemByCurso(@Param("curso") Curso curso);
}

