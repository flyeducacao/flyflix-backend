package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.aluno.*;
import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.auth.service.AlunoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Slf4j
@RestController
@RequestMapping("/alunos")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;
    @Autowired
    private AlunoRepository alunoRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> cadastrar(@RequestBody CadastroAluno dados) {
        return alunoService.cadastrarAluno(dados);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> atualizar(@RequestBody AtualizarAlunoRequest dados) {
        return alunoService.atualizarAluno(dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> remover(@PathVariable Long id) {
        return alunoService.removerAluno(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obter(@PathVariable Long id) {
        return alunoService.obterAluno(id);
    }

    @GetMapping
    public ResponseEntity<Page<AlunoResumoDTO>> listar(Pageable paginacao) {
        try {
            Page<AlunoResumoDTO> alunos = alunoService.listarAlunosResumo(paginacao);
            return ResponseEntity.ok(alunos);
        } catch (Exception e) {
            log.error("Erro ao listar alunos", e);
            // Retornar corpo vazio ou mensagem customizada:
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty(paginacao));
        }
    }
    @GetMapping("/por-data-cadastro")
    public ResponseEntity<List<AlunoResumoDTO>> listarPorDataCadastro(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim) {
        List<AlunoResumoDTO> alunos = alunoService.listarPorDataCadastro(dataInicio, dataFim);
        return ResponseEntity.ok(alunos);
    }
    @PostMapping("/{id}/matricular")
    public ResponseEntity<MatriculaResponseDTO> matricular(
            @PathVariable Long id,
            @RequestBody Set<Long> cursoIds
    ) {
        MatricularAlunoRequest request = new MatricularAlunoRequest(id, cursoIds);
        return alunoService.matricularAluno(request);
    }

    @PostMapping("/matricula-em-lote")
    public ResponseEntity<?> matriculaEmLote(@RequestBody MatriculaEmLoteRequest request) {
        return alunoService.matricularAlunosEmLote(request);
    }
    @GetMapping("/por-curso/{cursoId}")
    public ResponseEntity<List<AlunoResumoDTO>> listarPorCurso(@PathVariable Long cursoId) {
        return alunoService.listarAlunosPorCurso(cursoId);
    }




}
