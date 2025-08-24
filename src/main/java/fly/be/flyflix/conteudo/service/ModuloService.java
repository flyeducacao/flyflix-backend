package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.conteudo.dto.modulo.AtualizacaoModulo;
import fly.be.flyflix.conteudo.dto.modulo.CadastroModulo;
import fly.be.flyflix.conteudo.dto.modulo.DetalhamentoModulo;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.ModuloRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuloService {

    @Autowired
    private ModuloRepository moduloRepository;

    public DetalhamentoModulo cadastrar(CadastroModulo dados) {
        String tituloNormalizado = dados.titulo().trim();

        boolean existe = moduloRepository.existsByTituloIgnoreCase(tituloNormalizado);
        if (existe) {
            throw new BadRequestException("Já existe um módulo com o título '" + tituloNormalizado + "'");
        }

        Modulo modulo = new Modulo();
        modulo.setTitulo(tituloNormalizado);

        try {
            Modulo response = moduloRepository.save(modulo);
            return new DetalhamentoModulo(response);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Já existe um módulo com o título '" + tituloNormalizado + "'");
        }
    }



    public void atualizar(AtualizacaoModulo dados) {
        Modulo modulo = findByIdOrThrowsNotFoundException(dados.id());
        modulo.setTitulo(dados.titulo());

        moduloRepository.save(modulo);
    }

    public void remover(Long id) {
        Modulo modulo = findByIdOrThrowsNotFoundException(id);

        moduloRepository.delete(modulo);
    }

    public List<DetalhamentoModulo> listar() {
        return moduloRepository.findAll()
                .stream()
                .map(DetalhamentoModulo::new)
                .toList();
    }

    @Transactional
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
