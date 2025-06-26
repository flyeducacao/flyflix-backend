package fly.be.flyflix.auth.repository;

import fly.be.flyflix.auth.entity.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    List<Aluno> findByDataCadastroBetweenAndAtivoIsTrue(LocalDate dataCadastroAfter, LocalDate dataCadastroBefore);

    Optional<Aluno> findByIdAndAtivoIsTrue(Long id);

    Page<Aluno> findAllByAtivoIsTrue(Pageable pageable);
}
