package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.conteudo.dto.aula.CadastroAulaSemOrdem;
import fly.be.flyflix.conteudo.dto.aula.DadosAtualizacaoAula;
import fly.be.flyflix.conteudo.dto.aula.DadosDetalhamentoAula;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
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
    public ResponseEntity<Void> cadastrar(@RequestBody @Valid CadastroAulaSemOrdem dados) {
        aulaService.cadastrar(dados);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoAula>> listar() {
        List<DadosDetalhamentoAula> response = aulaService.listar();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload da capa da aula")
    @ApiResponse(responseCode = "200", description = "Imagem salva com sucesso.")
    @PostMapping(value = "/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<String> uploadCapa(
            @PathVariable Long id,
            @Parameter(description = "Imagem da capa", required = true)
            @RequestParam("imagem") MultipartFile imagem) throws Exception {
        aulaService.uploadCapa(id, imagem);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/capa")
    public ResponseEntity<byte[]> getCapa(@PathVariable Long id) {
        byte[] response = aulaService.getCapa(id);

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(response);
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

