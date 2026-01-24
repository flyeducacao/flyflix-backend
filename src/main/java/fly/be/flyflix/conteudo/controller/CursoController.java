package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.auth.service.AlunoService;
import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.dto.curso.DetalhamentoCurso;
import fly.be.flyflix.conteudo.dto.modulo.ModuloByListarPorCursoComOrdem;
import fly.be.flyflix.conteudo.service.CursoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private AlunoService alunoService;

    @PostMapping
    public ResponseEntity<DetalhamentoCurso> cadastrar(@RequestBody @Valid CadastroCurso dados, Authentication authentication) {
        Long requestingUserId = Long.valueOf(authentication.getName());
        DetalhamentoCurso response = cursoService.cadastrarCurso(dados, requestingUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<DetalhamentoCurso>> listar(@ParameterObject Pageable paginacao) {

        // Validação simples do sort
        Pageable pageable;
        try {
            paginacao.getSort().forEach(order -> {
                if (!order.getProperty().equals("id") && !order.getProperty().equals("titulo")) {
                    throw new IllegalArgumentException("Campo de ordenação inválido: " + order.getProperty());
                }
            });
            pageable = paginacao;
        } catch (Exception e) {
            pageable = PageRequest.of(paginacao.getPageNumber(), paginacao.getPageSize(), Sort.by("titulo"));
        }

        Page<DetalhamentoCurso> response = cursoService.listar(pageable);
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
    public ResponseEntity<DetalhamentoCurso> adicionarModulo(@PathVariable Long cursoId, @PathVariable Long moduloId) {
        DetalhamentoCurso response = cursoService.adicionarModuloAoCurso(cursoId, moduloId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idCurso}/modulos/{idModulo}")
    public ResponseEntity<String> adicionarOuAlterarOrdemModuloAoCurso(
            @PathVariable Long idCurso,
            @PathVariable Long idModulo,
            @RequestParam(required = false) Integer ordem
    ) {
        cursoService.adicionarOuAtualizarModuloNoCurso(idCurso, idModulo, ordem);
        return ResponseEntity.ok("Módulo adicionado ou ordem atualizada com sucesso.");
    }

    @GetMapping("/{id}/modulos")
    public ResponseEntity<List<ModuloByListarPorCursoComOrdem>> listarModulosPorCurso(@PathVariable Long id) {
        List<ModuloByListarPorCursoComOrdem> resposta = cursoService.listarModulosPorCursoComOrdem(id);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idCurso}/modulos/{idModulo}")
    public ResponseEntity<Void> removerModuloDoCurso(@PathVariable Long idCurso, @PathVariable Long idModulo) {
        cursoService.removerModulo(idCurso, idModulo);
        return ResponseEntity.noContent().build();
    }
}