package fly.be.flyflix.conteudo.controller;
import fly.be.flyflix.conteudo.dto.aula.AulasByListarModulos;
import fly.be.flyflix.conteudo.dto.modulo.AtualizacaoModulo;
import fly.be.flyflix.conteudo.dto.modulo.CadastroModulo;
import fly.be.flyflix.conteudo.dto.modulo.DetalhamentoModulo;
import fly.be.flyflix.conteudo.service.AulaService;
import fly.be.flyflix.conteudo.service.ModuloService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modulos")
public class ModuloController {

    @Autowired
    private ModuloService service;
    @Autowired
    private AulaService aulaService;
    @Autowired
    private ModuloService moduloService;

    @PostMapping
    public ResponseEntity<DetalhamentoModulo> cadastrar(@RequestBody @Valid CadastroModulo dados) {
        DetalhamentoModulo response = moduloService.cadastrar(dados);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DetalhamentoModulo>> listar() {
        List<DetalhamentoModulo> response = service.listar();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhamentoModulo> detalhar(@PathVariable Long id) {
        DetalhamentoModulo response = service.detalhar(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Void> atualizar(@RequestBody @Valid AtualizacaoModulo dados) {
        service.atualizar(dados);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/aulas")
    public ResponseEntity<List<AulasByListarModulos>> listarAulasPorModuloId(@PathVariable Long id) {
        List<AulasByListarModulos> response = aulaService.listarPorModuloId(id);

        return ResponseEntity.ok(response);
    }
}
