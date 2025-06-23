package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.FotoUploadDTO;
import fly.be.flyflix.auth.controller.dto.MensagemRespostaDTO;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Upload da foto de perfil do usuário.
     */
    @Operation(
            summary = "Upload da foto de perfil do usuário",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = FotoUploadDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Foto atualizada com sucesso."),
                    @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensagemRespostaDTO> uploadFoto(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @ModelAttribute FotoUploadDTO dto) throws IOException {

        MultipartFile imagem = dto.getImagem();

        var tipo = imagem.getContentType();
        if (tipo == null || !(tipo.equals("image/jpeg") || tipo.equals("image/png"))) {
            return ResponseEntity.badRequest().body(new MensagemRespostaDTO(
                    "Tipo de arquivo não permitido. Envie JPEG ou PNG.",
                    false,
                    400,
                    "VALIDACAO_ARQUIVO"
            ));
        }

        if (imagem.isEmpty()) {
            return ResponseEntity.badRequest().body(new MensagemRespostaDTO(
                    "Arquivo está vazio.",
                    false,
                    400,
                    "ARQUIVO_VAZIO"
            ));
        }

        if (imagem.getSize() > 300 * 1024) {
            return ResponseEntity.badRequest().body(new MensagemRespostaDTO(
                    "Tamanho excedido. Envie uma imagem de até 300 KB.",
                    false,
                    400,
                    "TAMANHO_EXCEDIDO"
            ));
        }

        var usuario = usuarioService.findByIdOrThrowsNotFoundException(id);

        usuario.setFotoPerfil(imagem.getBytes());
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new MensagemRespostaDTO(
                "Foto de perfil atualizada com sucesso.",
                true,
                200,
                null
        ));
    }


    /**
     * Download da foto de perfil do usuário.
     */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> downloadFoto(@PathVariable Long id) {
        try {
            byte[] imagem = usuarioService.obterFoto(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // ajuste conforme o tipo salvo
            return new ResponseEntity<>(imagem, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Remoção da foto de perfil do usuário.
     */
    @DeleteMapping("/{id}/foto")
    public ResponseEntity<MensagemRespostaDTO> removerFoto(@PathVariable Long id) {
        try {
            usuarioService.removerFoto(id);
            return ResponseEntity.ok(new MensagemRespostaDTO(
                    "Foto de perfil removida com sucesso.",
                    true,
                    200,
                    null
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensagemRespostaDTO(
                    e.getMessage(),
                    false,
                    404,
                    "USUARIO_NAO_ENCONTRADO"
            ));
        }
    }
}
