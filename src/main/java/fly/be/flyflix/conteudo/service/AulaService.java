package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.conteudo.dto.aula.CadastroAulaSemOrdem;
import fly.be.flyflix.conteudo.dto.aula.DadosAtualizacaoAula;
import fly.be.flyflix.conteudo.dto.aula.DadosDetalhamentoAula;
import fly.be.flyflix.conteudo.entity.Aula;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.AulaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AulaService {

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private ModuloService moduloService;

    public void cadastrar(CadastroAulaSemOrdem dados) {
        System.out.println(dados);
        var modulo = moduloService.findByIdOrThrowsNotFoundException(dados.moduloId());
        System.out.println(modulo);

        Integer maiorOrdem = aulaRepository.findMaxOrdemByModuloId(modulo.getId());
        int novaOrdem = maiorOrdem != null ? maiorOrdem + 1 : 1;
        System.out.println(maiorOrdem);

        var aula = Aula.builder()
                .titulo(dados.titulo())
                .tipo(dados.tipo())
                .ordem(novaOrdem)
                .duracaoEstimada(dados.duracaoEstimada())
                .linkConteudo(dados.linkConteudo())
                .modulo(modulo)
                .build();
        System.out.println(aula);

        aulaRepository.save(aula);
    }

    public List<DadosDetalhamentoAula> listar() {
        List<Aula> aulas = aulaRepository.findAll();

        return aulas.stream().map(aula ->
                new DadosDetalhamentoAula(
                        aula.getId(),
                        aula.getTitulo(),
                        aula.getTipo(),
                        aula.getOrdem(),
                        aula.getDuracaoEstimada(),
                        aula.getLinkConteudo(),
                        aula.getModulo() != null ? aula.getModulo().getId() : null,
                        "/api/aulas/" + aula.getId() + "/capa"
                )
        ).toList();
    }

    public List<Aula> listarPorModulo(Long moduloId) {
        return aulaRepository.findByModuloId(moduloId);
    }

    @Transactional
    public Aula atualizar(Long id, DadosAtualizacaoAula dados) {
        Aula aula = findByIdOrThrowsNotFoundException(id);

        aula.setTitulo(dados.titulo());
        aula.setTipo(dados.tipo());
        aula.setOrdem(dados.ordem());
        aula.setDuracaoEstimada(dados.duracaoEstimada());
        aula.setLinkConteudo(dados.linkConteudo());

        Long moduloId = dados.moduloId();
        if (moduloId != null) {
            Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(moduloId);
            aula.setModulo(modulo);
        } else {
            aula.setModulo(null); // desvincular da aula, se quiser
        }

        return aulaRepository.save(aula);
    }

    @Transactional
    public void remover(Long id) {
        aulaRepository.delete(findByIdOrThrowsNotFoundException(id));
    }

    public Aula detalhar(Long id) {
        return findByIdOrThrowsNotFoundException(id);
    }

    public Aula findByIdOrThrowsNotFoundException(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aula com id '%s' não encontrado".formatted(id)));
    }

    public void uploadCapa(Long id, MultipartFile imagem) throws IOException {
        var aula = findByIdOrThrowsNotFoundException(id);

        var tipo = imagem.getContentType();
        if (tipo == null || !(tipo.equals("image/jpeg") || tipo.equals("image/png"))) {
            throw new BadRequestException("Tipo de imagem inválido (JPEG ou PNG)");
        }

        aula.setCapa(imagem.getBytes());
        aulaRepository.save(aula);
    }
}