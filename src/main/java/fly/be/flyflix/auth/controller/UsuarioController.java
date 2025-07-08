package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.*;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioByGetMe> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        long usuarioId = Long.parseLong(authentication.getName());
        UsuarioByGetMe response = usuarioService.getMe(usuarioId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Salva a URL da foto de perfil do usuário (upload feito pelo frontend)",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{ \"fotoPerfilUrl\": \"https://firebasestorage.googleapis.com/...\" }")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Foto atualizada com sucesso."),
                    @ApiResponse(responseCode = "400", description = "URL inválida ou ausente."),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado.")
            }
    )
    @PostMapping("/{id}/foto")
    public ResponseEntity<MensagemRespostaDTO> salvarFotoUrl(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody AdicionarFotoDePerfilDto request) {

        usuarioService.salvarUrlFoto(id, request);

        return ResponseEntity.ok(new MensagemRespostaDTO(
                "Foto de perfil atualizada com sucesso.",
                true,
                200,
                null
        ));
    }

    /**
     * Retorna a URL da foto de perfil do usuário.
     */
    @GetMapping("/{id}/foto")
    public ResponseEntity<GetFotoPerfilDto> getFotoUrl(@PathVariable Long id) {
        GetFotoPerfilDto response = usuarioService.obterUrlFoto(id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/foto")
    public ResponseEntity<Void> atualizarFoto(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody AlteraFotoPerfilDto request) {
        usuarioService.atualizarFoto(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<MensagemRespostaDTO> removerFoto(@PathVariable Long id) {
        usuarioService.removerFoto(id);
        return ResponseEntity.ok(new MensagemRespostaDTO(
                "Foto de perfil removida com sucesso.",
                true,
                200,
                null
        ));
    }
}