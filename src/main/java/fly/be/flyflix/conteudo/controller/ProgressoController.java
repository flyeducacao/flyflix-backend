package fly.be.flyflix.conteudo.controller;


import fly.be.flyflix.conteudo.dto.progresso.ProgressoRequestDTO;
import fly.be.flyflix.conteudo.dto.progresso.ProgressoResponseDTO;
import fly.be.flyflix.conteudo.service.ProgressoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progresso")
public class ProgressoController {
    @Autowired
    private ProgressoService progressoService;

    @PostMapping("/marcar-assistida")
    public ResponseEntity<?> marcarAssistida(@RequestBody ProgressoRequestDTO dto) {
        progressoService.marcarComoAssistida(dto.getAlunoId(), dto.getAulaId(), dto.getCursoId());
        return ResponseEntity.ok("Progresso registrado com sucesso.");
    }
    @GetMapping("/status")
    public ResponseEntity<ProgressoResponseDTO> obterProgresso(
            @RequestParam Long alunoId,
            @RequestParam Long cursoId) {
        ProgressoResponseDTO progresso = progressoService.obterProgresso(alunoId, cursoId);
        return ResponseEntity.ok(progresso);
    }



}

