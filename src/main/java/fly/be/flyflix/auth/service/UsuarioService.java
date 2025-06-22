package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.auth.repository.PasswordResetTokenRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository, PasswordResetTokenRepository tokenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.tokenRepository = tokenRepository;
    }


    // ========================================
    // SENHAS
    // ========================================

    public ResponseEntity<Map<String, String>> resetarSenha(String login) {
        return usuarioRepository.findByEmail(login)
                .map(usuario -> {
                    String novaSenha = UUID.randomUUID().toString().substring(0, 8);
                    usuario.setSenha(passwordEncoder.encode(novaSenha));
                    usuarioRepository.save(usuario);
                    emailService.enviarEmail(
                            usuario.getEmail(),
                            "Redefinição de senha FlyFlix",
                            "Sua nova senha temporária é:\n\n" + novaSenha + "\n\nAltere-a após o login."
                    );
                    return ResponseEntity.ok(Map.of("message", "Nova senha enviada por email"));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Usuário não encontrado")));
    }
    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "image/jpeg",
            "image/png"
    );


    private static final long TAMANHO_MAXIMO = 300 * 1024; // 300KB

    public void salvarFoto(Long id, MultipartFile arquivo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("Nenhum arquivo foi enviado.");
        }

        String tipo = arquivo.getContentType();
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido. Envie JPEG ou PNG.");
        }

        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo está vazio.");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Tamanho excedido. Envie uma imagem de até 1MB.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setFotoPerfil(arquivo.getBytes());
        usuarioRepository.save(usuario);
    }




    public byte[] obterFoto(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getFotoPerfil() != null) {
            return usuario.getFotoPerfil();
        }

        try {
            // Caminho do recurso dentro de src/main/resources
            InputStream inputStream = getClass().getResourceAsStream("/static/imagens/sem-foto.jpg");
            if (inputStream == null) throw new FileNotFoundException("Imagem padrão não encontrada");
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar imagem padrão");
        }
    }

    public void removerFoto(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);
    }

}




