package fly.be.flyflix.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;


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

