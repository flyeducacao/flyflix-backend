package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.UsuarioByGetMe;
import fly.be.flyflix.auth.entity.Admin;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.auth.repository.PasswordResetTokenRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository, PasswordResetTokenRepository tokenRepository) {
        this.usuarioRepository = usuarioRepository;
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
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
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

        Usuario usuario = findByIdOrThrowsNotFoundException(id);

        usuario.setFotoPerfil(arquivo.getBytes());
        usuarioRepository.save(usuario);
    }

    public UsuarioByGetMe getMe(Long usuarioId) {
        Usuario usuario = findByIdOrThrowsNotFoundException(usuarioId);

        return UsuarioByGetMe.by(usuario);
    }

    public byte[] obterFoto(Long id) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);

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
        Usuario usuario = findByIdOrThrowsNotFoundException(id);

        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);
    }

    public Usuario findByIdOrThrowsNotFoundException(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário com id '%s' não encontrado".formatted(id)));
    }

    public void assertEmailIsNotRegistered(String email) {
        usuarioRepository.findByEmail(email)
                .ifPresent(this::throwsEmailJaCadastradoException);
    }

    public void assertEmailIsNotRegistered(String email, Usuario usuario) {
        usuarioRepository.findByEmailAndIdIsNot(email, usuario.getId())
                .ifPresent(this::throwsEmailJaCadastradoException);
    }

    public void assertCpfDoesNotBelongsToAnotherUser(String cpf) {
        usuarioRepository.findByCpf(cpf)
                .ifPresent(this::throwsCpfJaCadastradoException);
    }

    public void assertCpfDoesNotBelongsToAnotherUser(String cpf, Usuario usuario) {
        usuarioRepository.findByCpfAndIdIsNot(cpf, usuario.getId())
                .ifPresent(this::throwsCpfJaCadastradoException);
    }

    private void throwsCpfJaCadastradoException(Usuario usuario) {
        throw new BadRequestException("O cpf '%s' já está cadastrado".formatted(usuario.getCpf()));
    }

    public void throwsEmailJaCadastradoException(Usuario usuario) {
        throw new BadRequestException("O email '%s' já está cadastrado".formatted(usuario.getEmail()));
    }
}




