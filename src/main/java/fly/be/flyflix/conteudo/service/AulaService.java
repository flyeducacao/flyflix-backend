package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.conteudo.dto.aula.CadastroAulaSemOrdem;
import fly.be.flyflix.conteudo.dto.aula.DadosAtualizacaoAula;
import fly.be.flyflix.conteudo.dto.aula.DadosDetalhamentoAula;
import fly.be.flyflix.conteudo.entity.Aula;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.AulaRepository;
import jakarta.validation.Valid;
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
        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(dados.moduloId());

        Integer maiorOrdem = aulaRepository.findMaxOrdemByModuloId(modulo.getId());
        int novaOrdem = maiorOrdem != null ? maiorOrdem + 1 : 1;

        Aula aula = Aula.builder()
                .titulo(dados.titulo())
                .tipo(dados.tipo())
                .ordem(novaOrdem)
                .duracaoEstimada(dados.duracaoEstimada())
                .linkConteudo(dados.linkConteudo())
                .modulo(modulo)
                .build();

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

    public Aula findByIdOrThrowsNotFoundException(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aula com id '%s' não encontrado".formatted(id)));
    }

    public void uploadCapa(Long id, MultipartFile imagem) throws IOException {
        Aula aula = findByIdOrThrowsNotFoundException(id);

        String tipo = imagem.getContentType();
        if (tipo == null || !(tipo.equals("image/jpeg") || tipo.equals("image/png"))) {
            throw new BadRequestException("Tipo de imagem inválido (JPEG ou PNG)");
        }

        aula.setCapa(imagem.getBytes());
        aulaRepository.save(aula);
    }

    public byte[] getCapa(Long aulaId) {
        Aula aula = findByIdOrThrowsNotFoundException(aulaId);
        byte[] capa = aula.getCapa();

        if (capa == null) throw new NotFoundException("Capa não encontrada");

        return capa;
    }

    public void atualizar(DadosAtualizacaoAula dados) {
        Aula aula = findByIdOrThrowsNotFoundException(dados.id());

        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(dados.moduloId());

        aula.setTitulo(dados.titulo());
        aula.setTipo(dados.tipo());
        aula.setOrdem(dados.ordem());
        aula.setDuracaoEstimada(dados.duracaoEstimada());
        aula.setLinkConteudo(dados.linkConteudo());
        aula.setModulo(modulo);

        aulaRepository.save(aula);
    }
}