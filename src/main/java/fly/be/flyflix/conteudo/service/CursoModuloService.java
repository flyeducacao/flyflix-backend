package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.CursoModuloRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CursoModuloService {
    private final CursoModuloRepository cursoModuloRepository;

    public CursoModulo findByCursoAndModuloOrThrowsNotFoundException(Curso curso, Modulo modulo) {
        return cursoModuloRepository.findByCursoAndModulo(curso, modulo)
                .orElseThrow(() -> new NotFoundException("Associação entre curso e módulo não encontrada."));
    }

    @Transactional
    public void remover(CursoModulo cursoModulo) {
        cursoModuloRepository.delete(cursoModulo);
    }
}
