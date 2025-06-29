package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.senha.RedefinicaoSenhaDTO;
import fly.be.flyflix.auth.controller.dto.senha.RequisicaoResetSenhaDTO;
import fly.be.flyflix.auth.entity.PasswordResetToken;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.PasswordResetTokenRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
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
            throw new BadRequestException("Email não encontrado");
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
        PasswordResetToken token = tokenRepository.findByToken(dto.token())
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expirado");
        }

        Usuario usuario = token.getUsuario();
        usuario.setSenha(dto.novaSenha()); // idealmente aplicar hash
        tokenRepository.delete(token);

        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
    }
}
