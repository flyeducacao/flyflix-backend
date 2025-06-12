package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.RedefinicaoSenhaDTO;
import fly.be.flyflix.auth.controller.dto.RequisicaoResetSenhaDTO;
import fly.be.flyflix.auth.entity.Admin;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.entity.PasswordResetToken;
import fly.be.flyflix.auth.repository.PasswordResetTokenRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder; // hash da senha


    public ResponseEntity<Map<String, Object>> solicitarResetSenha(RequisicaoResetSenhaDTO dto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.email());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email não encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        // Gerar token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setExpirationDate(LocalDateTime.now().plusHours(2));
        tokenRepository.save(resetToken);

        // Enviar e-mail com link
        String link = "http://localhost:3000/redefinir-senha?token=" + token; // ajuste o link real
        emailService.enviarEmail(
                usuario.getEmail(),
                "Redefinição de senha",
                "Olá, clique no link abaixo para redefinir sua senha:\n" + link
        );

        return ResponseEntity.ok(Map.of("message", "E-mail de redefinição enviado com sucesso"));
    }


    public ResponseEntity<Map<String, Object>> redefinirSenha(RedefinicaoSenhaDTO dto) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(dto.token());

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token inválido"));
        }
        PasswordResetToken token = tokenOpt.get();
        if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token expirado"));
        }
        Usuario usuario = token.getUsuario();
        usuario.setSenha(dto.novaSenha()); // idealmente aplicar hash
        tokenRepository.delete(token);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
    }
}
