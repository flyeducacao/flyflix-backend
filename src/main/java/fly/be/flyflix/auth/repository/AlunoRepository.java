package fly.be.flyflix.auth.repository;

import fly.be.flyflix.auth.entity.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Page<Aluno> findAllByAtivoTrue(Pageable paginacao);
    Optional<Aluno> findById(Long id);
    Optional <Aluno> findByEmail(String email);
    List<Aluno> findByDataCadastroBetween(LocalDate dataInicio, LocalDate dataFim);

}
