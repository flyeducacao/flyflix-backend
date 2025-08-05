package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.auth.service.AlunoService;
import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.dto.curso.DetalhamentoCurso;
import fly.be.flyflix.conteudo.dto.modulo.ModuloByListarPorCursoComOrdem;
import fly.be.flyflix.conteudo.service.CursoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    public ResponseEntity<String> removerModuloDoCurso(@PathVariable Long idCurso, @PathVariable Long idModulo) {
        cursoService.removerModulo(idCurso, idModulo);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/{id}/alunos/importar/xlsx",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> matricularAlunosViaXlsx(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        byte[] response = alunoService.matricularAlunosViaXlsx(id, file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename("resultado-importação-matricula-%s.xlsx".formatted(LocalDateTime.now()))
                        .build()
        );

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }
}