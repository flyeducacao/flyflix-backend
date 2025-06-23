package fly.be.flyflix.conteudo.controller;
import fly.be.flyflix.conteudo.dto.modulo.AtualizacaoModulo;
import fly.be.flyflix.conteudo.dto.modulo.CadastroModulo;
import fly.be.flyflix.conteudo.dto.modulo.DetalhamentoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.repository.ModuloRepository;
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

import java.net.URI;

@RestController
@RequestMapping("/api/modulos")
public class ModuloController {

    @Autowired
    private ModuloService service;
    @Autowired
    private ModuloRepository moduloRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<DetalhamentoModulo> cadastrar(@RequestBody @Valid CadastroModulo dados) {
        Modulo modulo = new Modulo();
        modulo.setTitulo(dados.titulo());
        moduloRepository.save(modulo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DetalhamentoModulo(modulo.getId(), modulo.getTitulo())); // ordem será definida no curso
    }

    @GetMapping
    public Page<DetalhamentoModulo> listar(@PageableDefault(size = 10) Pageable paginacao) {
        return service.listar(paginacao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhamentoModulo> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Modulo> atualizar(@RequestBody @Valid AtualizacaoModulo dados) {
        return ResponseEntity.ok(service.atualizar(dados));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
