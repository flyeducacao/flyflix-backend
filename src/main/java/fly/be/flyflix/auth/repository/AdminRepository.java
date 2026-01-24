package fly.be.flyflix.auth.repository;


import fly.be.flyflix.auth.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByIdAndAtivoIsTrue(Long id);

    Page<Admin> findAllByAtivoIsTrue(Pageable pageable);
}


