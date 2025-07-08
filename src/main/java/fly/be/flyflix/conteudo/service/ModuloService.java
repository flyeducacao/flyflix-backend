package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.conteudo.dto.modulo.AtualizacaoModulo;
import fly.be.flyflix.conteudo.dto.modulo.CadastroModulo;
import fly.be.flyflix.conteudo.dto.modulo.DetalhamentoModulo;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.ModuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuloService {

    @Autowired
    private ModuloRepository moduloRepository;

    public DetalhamentoModulo cadastrar(CadastroModulo dados) {
        Modulo modulo = new Modulo();
        modulo.setTitulo(dados.titulo());

        Modulo response = moduloRepository.save(modulo);

        return new DetalhamentoModulo(response);
    }

    public void atualizar(AtualizacaoModulo dados) {
        Modulo modulo = findByIdOrThrowsNotFoundException(dados.id());
        modulo.setTitulo(dados.titulo());

        moduloRepository.save(modulo);
    }

    @Transactional
    public void remover(Long id) {
        moduloRepository.deleteById(id);
    }

    public Page<DetalhamentoModulo> listar(Pageable paginacao) {
        return moduloRepository.findAll(paginacao)
                .map(DetalhamentoModulo::new);
    }

    public DetalhamentoModulo detalhar(Long id) {
        Modulo modulo = findByIdOrThrowsNotFoundException(id);
        return new DetalhamentoModulo(modulo);
    }

    public Modulo findByIdOrThrowsNotFoundException(Long id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Módulo com id '%s' não encontrado".formatted(id)));
    }

    public List<Modulo> listarPorCurso(Curso curso) {
        return moduloRepository.findByCursoId(curso.getId());
    }
}
