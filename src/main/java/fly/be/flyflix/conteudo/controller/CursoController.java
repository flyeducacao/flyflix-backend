package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.dto.curso.DetalhamentoCurso;
import fly.be.flyflix.conteudo.dto.modulo.ModuloByListarPorCurso;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.CursoModuloRepository;
import fly.be.flyflix.conteudo.repository.CursoRepository;
import fly.be.flyflix.conteudo.repository.ModuloRepository;
import fly.be.flyflix.conteudo.service.CursoService;
import fly.be.flyflix.conteudo.service.ModuloService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {
    @Autowired
    private CursoModuloRepository cursoModuloRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private ModuloRepository moduloRepository;
    @Autowired
    private CursoService cursoService;
    @Autowired
    private ModuloService moduloService;

    @PostMapping
    public ResponseEntity<DetalhamentoCurso> cadastrar(@RequestBody @Valid CadastroCurso dados, Authentication authentication) {
        Long requestingUserId = Long.valueOf(authentication.getName());

        DetalhamentoCurso response = cursoService.cadastrarCurso(dados, requestingUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<DetalhamentoCurso>> listar(@PageableDefault(size = 10, sort = "titulo") Pageable paginacao) {
        Page<DetalhamentoCurso> response = cursoService.listar(paginacao);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<DetalhamentoCurso> detalhar(@PathVariable Long id) {
        DetalhamentoCurso response = cursoService.detalhar(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalhamentoCurso> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoCurso dados) {
        cursoService.atualizarCurso(id, dados);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cursoService.remover(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cursoId}/modulos/{moduloId}")
    public ResponseEntity<?> adicionarModulo(@PathVariable Long cursoId, @PathVariable Long moduloId) {
        DetalhamentoCurso response = cursoService.adicionarModuloAoCurso(cursoId, moduloId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idCurso}/modulos/{idModulo}")
    @Transactional
    public ResponseEntity<String> adicionarOuAlterarOrdemModuloAoCurso(
            @PathVariable Long idCurso,
            @PathVariable Long idModulo,
            @RequestParam(required = false) Integer ordem
    ) {
        cursoService.adicionarOuAtualizarModuloNoCurso(idCurso, idModulo, ordem);

        return ResponseEntity.ok("Módulo adicionado ou ordem atualizada com sucesso.");
    }

    @GetMapping("/{id}/modulos")
    public ResponseEntity<List<ModuloByListarPorCurso>> listarModulosPorCurso(@PathVariable Long id) {
        List<ModuloByListarPorCurso> resposta = cursoService.listarModulosPorCurso(id);

        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idCurso}/modulos/{idModulo}")
    public ResponseEntity<String> removerModuloDoCurso(@PathVariable Long idCurso, @PathVariable Long idModulo) {
        cursoService.removerModulo(idCurso, idModulo);

        return ResponseEntity.noContent().build();
    }
}