package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.LoginRequest;
import fly.be.flyflix.auth.controller.dto.LoginResponse;
import fly.be.flyflix.auth.entity.RefreshToken;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.RefreshTokenRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioService usuarioService;

    private final Set<String> tokenBlacklist = new HashSet<>();

    @Value("${jwt.expiration-minutes:15}")
    private long jwtExpirationMinutes;

    @Value("${jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    public TokenService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            UsuarioService usuarioService
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * 🔐 LOGIN — autentica o usuário e gera access + refresh tokens
     */
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.senha())
        );

        Usuario usuario = (Usuario) authentication.getPrincipal();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(jwtExpirationMinutes));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("flyflix-backend")
                .subject(usuario.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("authorities", List.of("ROLE_" + usuario.getRole()))
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        long expiresIn = Duration.ofMinutes(jwtExpirationMinutes).getSeconds();

        RefreshToken refreshToken = createRefreshToken(usuario.getId());

        return new LoginResponse(accessToken, expiresIn, refreshToken.getToken());
    }

    /**
     * ♻️ REFRESH TOKEN — gera um novo accessToken a partir do refreshToken
     */
    public LoginResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expirado ou revogado");
        }

        Usuario usuario = usuarioRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(jwtExpirationMinutes));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("flyflix-backend")
                .subject(usuario.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("authorities", List.of("ROLE_" + usuario.getRole()))
                .build();

        String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        long expiresIn = Duration.ofMinutes(jwtExpirationMinutes).getSeconds();

        return new LoginResponse(newAccessToken, expiresIn, refreshTokenValue);
    }

    /**
     * 🚪 LOGOUT — revoga o refresh token atual
     */
    public void revokeRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * 🔑 Cria um novo refresh token
     */
    private RefreshToken createRefreshToken(Long userId) {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(refreshExpirationDays));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    /**
     * 🔒 Gera token de redefinição de senha (independente dos tokens de login)
     */
    public String gerarTokenRedefinicaoSenha(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(1));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("flyflix-password-reset")
                .subject(usuario.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("type", "password-reset")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * ✅ Valida o token de redefinição de senha
     */
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

    /**
     * ❌ Invalida manualmente um token (ex: reset password usado)
     */
    public void invalidarToken(String token) {
        tokenBlacklist.add(token);
    }
}

