package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.LoginRequest;
import fly.be.flyflix.auth.controller.dto.LoginResponse;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.auth.service.EmailService;
import fly.be.flyflix.auth.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(TokenService tokenService,
                          UsuarioRepository usuarioRepository,
                          EmailService emailService,
                          PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;

        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
//senhas
    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> esqueciSenha(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email não cadastrado.");
        }

        Usuario usuario = usuarioOpt.get();
        String token = tokenService.gerarTokenRedefinicaoSenha(usuario);

        String link = "http://localhost:3000/resetar-senha?token=" + token;
        String conteudoHtml = """
    <p>Olá,</p>
    <p>Para redefinir sua senha, <a href="%s">clique aqui</a>.</p>
    <p>Se você não solicitou essa alteração, ignore este e-mail.</p>
""".formatted(link);

        emailService.enviarEmail(email, "Redefinição de senha Flyflix", conteudoHtml);


//        emailService.enviarEmail(email, "Redefinição de senha Flyflix",
//                "Use esse link para redefinir sua senha: " + link);

        return ResponseEntity.ok("Email enviado para redefinição de senha.");
    }



    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetarSenha(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String novaSenha = body.get("novaSenha");

        // Validação da nova senha
        if (!isSenhaValida(novaSenha)) {
            return ResponseEntity.badRequest().body("A senha deve ter no mínimo 8 caracteres, uma letra maiúscula, um número e um caractere especial.");
        }

        try {
            Usuario usuario = tokenService.validarTokenRedefinicaoSenha(token);
            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuarioRepository.save(usuario);
            tokenService.invalidarToken(token);
            return ResponseEntity.ok("Senha alterada com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
