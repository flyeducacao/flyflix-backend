package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.conteudo.dto.aula.CadastroAula;
import fly.be.flyflix.conteudo.dto.aula.DadosAtualizacaoAula;
import fly.be.flyflix.conteudo.dto.aula.DadosDetalhamentoAula;
import fly.be.flyflix.conteudo.entity.Aula;
import fly.be.flyflix.conteudo.repository.AulaRepository;
import fly.be.flyflix.conteudo.service.AulaService;
import fly.be.flyflix.conteudo.service.ModuloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
@RequestMapping("/api/aulas")
public class AulaController {
    @Autowired
    private AulaRepository aulaRepository;
    @Autowired
    private ModuloService moduloService;
    @Autowired
    private AulaService aulaService;

    @PostMapping
    @Transactional
    public ResponseEntity<Void> cadastrar(@RequestBody @Valid CadastroAula dados) {
        var modulo = moduloService.findByIdOrThrowsNotFoundException(dados.moduloId());

        var aula = Aula.builder()
                .titulo(dados.titulo())
                .tipo(dados.tipo())
                .ordem(dados.ordem())
                .duracaoEstimada(dados.duracaoEstimada())
                .linkConteudo(dados.linkConteudo())
                .modulo(modulo)
                .build();

        aulaRepository.save(aula);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoAula>> listar() {
        var aulas = aulaRepository.findAll().stream().map(aula ->
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

        return ResponseEntity.ok(aulas);
    }

    @Operation(summary = "Upload da capa da aula")
    @ApiResponse(responseCode = "200", description = "Imagem salva com sucesso.")
    @PostMapping(value = "/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<String> uploadCapa(
            @PathVariable Long id,
            @Parameter(description = "Imagem da capa", required = true)
            @RequestParam("imagem") MultipartFile imagem) throws Exception {

        var aula = aulaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada"));

        var tipo = imagem.getContentType();
        if (tipo == null || !(tipo.equals("image/jpeg") || tipo.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de imagem inválido (JPEG ou PNG)");
        }

        aula.setCapa(imagem.getBytes());
        aulaRepository.save(aula);
        return ResponseEntity.ok("Imagem da capa salva com sucesso.");
    }

    @GetMapping("/{id}/capa")
    public ResponseEntity<byte[]> getCapa(@PathVariable Long id) {
        var aula = aulaService.findByIdOrThrowsNotFoundException(id);

        if (aula.getCapa() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg") // opcional: salvar tipo MIME no banco para maior controle
                .body(aula.getCapa());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Void> atualizar(@RequestBody @Valid DadosAtualizacaoAula dados) {
        var aula = aulaService.findByIdOrThrowsNotFoundException(dados.id());

        var modulo = moduloService.findByIdOrThrowsNotFoundException(dados.moduloId());

        aula.setTitulo(dados.titulo());
        aula.setTipo(dados.tipo());
        aula.setOrdem(dados.ordem());
        aula.setDuracaoEstimada(dados.duracaoEstimada());
        aula.setLinkConteudo(dados.linkConteudo());
        aula.setModulo(modulo);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        aulaRepository.delete(aulaService.findByIdOrThrowsNotFoundException(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoAula> detalhar(@PathVariable Long id) {
        var aula = aulaService.findByIdOrThrowsNotFoundException(id);

        var dto = new DadosDetalhamentoAula(
                aula.getId(),
                aula.getTitulo(),
                aula.getTipo(),
                aula.getOrdem(),
                aula.getDuracaoEstimada(),
                aula.getLinkConteudo(),
                aula.getModulo() != null ? aula.getModulo().getId() : null,
                "/api/aulas/" + aula.getId() + "/capa"
        );
        return ResponseEntity.ok(dto);
    }
}

