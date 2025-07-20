package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.aluno.*;
import fly.be.flyflix.auth.service.AlunoService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
@Slf4j
@RestController
@RequestMapping("/alunos")
public class AlunoController {
    @Autowired
    private AlunoService alunoService;
    @Autowired
    private Validator validator;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Void> cadastrar(@RequestBody @Valid CadastroAluno dados) {
        alunoService.cadastrarAluno(dados);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoImportacaoAlunosDTO> importarAlunos(@RequestPart("file") MultipartFile file) {
        ResultadoImportacaoAlunosDTO resultado = alunoService.importarAlunosViaPlanilha(file);
        return ResponseEntity.ok(resultado);
    }
    @GetMapping("/importar/relatorio-erros")
    public ResponseEntity<byte[]> baixarErrosImportacao() {
        byte[] arquivo = alunoService.gerarRelatorioErros();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=erros-importacao.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }


    @PutMapping
    public ResponseEntity<Void> atualizar(@RequestBody AtualizarAlunoRequest dados) {
        alunoService.atualizarAluno(dados);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        alunoService.removerAluno(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObterAluno> obter(@PathVariable Long id) {
        ObterAluno response = alunoService.obterAluno(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AlunoResumoDTO>> listar(Pageable paginacao) {
        Page<AlunoResumoDTO> alunos = alunoService.listarAlunosResumo(paginacao);

        return ResponseEntity.ok(alunos);
    }
    @GetMapping("/por-data-cadastro")
    public ResponseEntity<List<AlunoResumoDTO>> listarPorDataCadastro(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim
    ) {
        List<AlunoResumoDTO> alunos = alunoService.listarPorDataCadastro(dataInicio, dataFim);

        return ResponseEntity.ok(alunos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/matricular")
    public ResponseEntity<MatriculaResponseDTO> matricular(
            @PathVariable Long id,
            @RequestBody Set<Long> cursoIds
    ) {
        MatricularAlunoRequest request = new MatricularAlunoRequest(id, cursoIds);

        MatriculaResponseDTO response = alunoService.matricularAluno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/matricula-em-lote")
    public ResponseEntity<Void> matriculaEmLote(@RequestBody @Valid MatriculaEmLoteRequest request) {
        alunoService.matricularAlunosEmLote(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/por-curso/{cursoId}")
    public ResponseEntity<List<AlunoResumoDTO>> listarPorCurso(@PathVariable Long cursoId) {
        List<AlunoResumoDTO> response = alunoService.listarAlunosPorCurso(cursoId);

        return ResponseEntity.ok(response);
    }


}
