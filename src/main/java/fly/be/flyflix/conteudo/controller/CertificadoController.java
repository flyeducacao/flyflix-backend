package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.conteudo.dto.certificado.CertificadoElegibilidadeDTO;
import fly.be.flyflix.conteudo.service.CertificadoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

    private final CertificadoService certificadoService;

    public CertificadoController(CertificadoService certificadoService) {
        this.certificadoService = certificadoService;
    }

    @GetMapping("/eligibilidade")
    public ResponseEntity<CertificadoElegibilidadeDTO> verificarElegibilidade(
            @RequestParam Long alunoId,
            @RequestParam Long cursoId) {

        CertificadoElegibilidadeDTO dto = certificadoService.verificarElegibilidade(alunoId, cursoId);

        if (dto.isElegivel()) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
        }
    }


    @GetMapping("/download")
    public ResponseEntity<byte[]> baixarCertificado(
            @RequestParam Long alunoId,
            @RequestParam Long cursoId
    ) {
        byte[] pdf = certificadoService.gerarCertificado(alunoId, cursoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificado.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
