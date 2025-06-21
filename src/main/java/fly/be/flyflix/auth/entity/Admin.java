package fly.be.flyflix.auth.entity;

import fly.be.flyflix.auth.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends Usuario {

    @Column(nullable = false)
    private Boolean ativo = true;
}

