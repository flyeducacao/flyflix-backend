package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.LoginRequest;
import fly.be.flyflix.auth.controller.dto.LoginResponse;
import fly.be.flyflix.auth.entity.Usuario;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TokenService {
    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    private final Set<String> tokenBlacklist = new HashSet<>();
    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;

    public TokenService(JwtDecoder jwtDecoder,
                        JwtEncoder jwtEncoder,
                        AuthenticationManager authenticationManager, UsuarioService usuarioService
    ) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.senha())
        );

        Usuario usuario = (Usuario) authentication.getPrincipal();
        Instant now = Instant.now();
        long expiresIn = Duration.ofDays(90).getSeconds();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("flyflix-backend")
                .subject(usuario.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("authorities", List.of("ROLE_" + usuario.getRole()));

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).getTokenValue();
        return new LoginResponse(token, expiresIn);
    }

    // ✅ Agora recebe qualquer tipo de Usuario, não apenas Aluno
    public String gerarTokenRedefinicaoSenha(Usuario usuario) {
        Instant now = Instant.now();
        long expiresIn = Duration.ofHours(1).getSeconds();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("flyflix-password-reset")
                .subject(usuario.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("type", "password-reset")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Usuario validarTokenRedefinicaoSenha(String token) {
        try {
            if (tokenBlacklist.contains(token)) {
                throw new RuntimeException("Token já foi utilizado ou está inválido");
            }

            Jwt decoded = jwtDecoder.decode(token);

            if (!"password-reset".equals(decoded.getClaimAsString("type"))) {
                throw new RuntimeException("Tipo de token inválido");
            }

            Long usuarioId = Long.parseLong(decoded.getSubject());

            return usuarioService.findByIdOrThrowsNotFoundException(usuarioId);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Token inválido ou expirado");
        }
    }

    public void invalidarToken(String token) {
        tokenBlacklist.add(token);
    }
}
