package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.entity.AlunoCurso;
import fly.be.flyflix.auth.repository.AlunoCursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlunoCursoService {
    private final AlunoCursoRepository repository;

    public AlunoCurso save(AlunoCurso alunoCurso) {
        return repository.save(alunoCurso);
    }
}
