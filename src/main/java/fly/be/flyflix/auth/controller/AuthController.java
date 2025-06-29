package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.LoginRequest;
import fly.be.flyflix.auth.controller.dto.LoginResponse;
import fly.be.flyflix.auth.controller.dto.MensagemRespostaDTO;
import fly.be.flyflix.auth.controller.dto.senha.AtualizarSenhaDTO;
import fly.be.flyflix.auth.controller.dto.senha.RedefinicaoSenhaDTO;
import fly.be.flyflix.auth.controller.dto.senha.RequisicaoResetSenhaDTO;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.auth.service.EmailService;
import fly.be.flyflix.auth.service.SenhaService;
import fly.be.flyflix.auth.service.TokenService;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SenhaService senhaService;


    public AuthController(TokenService tokenService,
                          UsuarioRepository usuarioRepository,
                          EmailService emailService,
                          PasswordEncoder passwordEncoder, SenhaService senhaService) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;

        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.senhaService = senhaService;
    }
//senhas
@PostMapping("/esqueci-senha")
public ResponseEntity<?> esqueciSenha(@RequestBody @Valid RequisicaoResetSenhaDTO dto) {
    String email = dto.email();

    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("Email não cadastrado."));

    String token = tokenService.gerarTokenRedefinicaoSenha(usuario);

    String link = "http://localhost:3000/resetar-senha?token=" + token;
    String conteudoHtml = """
        <p>Olá,</p>
        <p>Para redefinir sua senha, <a href="%s">clique aqui</a>.</p>
        <p>Se você não solicitou essa alteração, ignore este e-mail.</p>
    """.formatted(link);

    emailService.enviarEmail(email, "Redefinição de senha Flyflix", conteudoHtml);

    return ResponseEntity.ok(new MensagemRespostaDTO(
            "Email enviado com instruções para redefinir a senha.",
            true,
            HttpStatus.OK.value(),
            "EMAIL_RESET_SENHA_ENVIADO"
    ));
}
    @PutMapping("/atualizar-senha")
    public ResponseEntity<?> atualizarSenha(@RequestBody @Valid AtualizarSenhaDTO dto) {
        senhaService.atualizarSenha(dto);
        return ResponseEntity.ok(new MensagemRespostaDTO(
                "Senha atualizada com sucesso.", true, HttpStatus.OK.value(), "SENHA_ATUALIZADA"
        ));
    }

    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetarSenha(@RequestBody @Valid RedefinicaoSenhaDTO dto) {
        senhaService.redefinirSenha(dto);
        return ResponseEntity.ok(new MensagemRespostaDTO(
                "Senha redefinida com sucesso.", true, HttpStatus.OK.value(), "SENHA_REDEFINIDA"
        ));
    }

    private boolean isSenhaValida(String senha) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=<>?{}\\[\\]~]).{8,}$";
        return senha != null && senha.matches(regex);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = tokenService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

}
