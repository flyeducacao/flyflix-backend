package fly.be.flyflix.auth.repository;

import fly.be.flyflix.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpf(String cpf);

    Optional<Usuario> findByNome(String nome);
    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);


    Optional<Usuario> findByEmailAndIdIsNot(String email, Long id);

    Optional<Usuario> findByCpfAndId(String cpf, Long id);

    Optional<Usuario> findByCpfAndIdIsNot(String cpf, Long id);
}

