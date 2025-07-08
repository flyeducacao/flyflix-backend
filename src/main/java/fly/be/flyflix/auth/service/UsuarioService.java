package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.UsuarioByGetMe;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final String urlFotoDePerfilPadrao = "https://firebasestorage.googleapis.com/v0/b/flyeducation-1eea5.firebasestorage.app/o/userImg.jpg?alt=media&token=d5a70bb6-1589-4b55-bb70-9438108d7e37";

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ================================
    // FOTO DE PERFIL (apenas URL)
    // ================================

    public void salvarUrlFoto(Long id, String url) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);
        usuario.setFotoPerfilUrl(url);
        usuarioRepository.save(usuario);
    }

    public String obterUrlFoto(Long id) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);
        return usuario.getFotoPerfilUrl();
    }

    public void removerFoto(Long id) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);
        adicionarFotoDePerfilPadrao(usuario);
        usuarioRepository.save(usuario);
    }

    // ================================
    // DADOS DO USUÁRIO
    // ================================

    public UsuarioByGetMe getMe(Long usuarioId) {
        Usuario usuario = findByIdOrThrowsNotFoundException(usuarioId);

        // Retorna a URL real se houver, senão o campo virá como null
        return UsuarioByGetMe.by(usuario, usuario.getFotoPerfilUrl());
    }


    public Usuario findByIdOrThrowsNotFoundException(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário com id '%s' não encontrado".formatted(id)));
    }

    // ================================
    // SENHA
    // ================================

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

    public void adicionarFotoDePerfilPadrao(Usuario usuario) {
        usuario.setFotoPerfilUrl(urlFotoDePerfilPadrao);
    }

    // ================================
    // VALIDAÇÕES
    // ================================

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
        throw new BadRequestException("O CPF '%s' já está cadastrado".formatted(usuario.getCpf()));
    }

    public void throwsEmailJaCadastradoException(Usuario usuario) {
        throw new BadRequestException("O email '%s' já está cadastrado".formatted(usuario.getEmail()));
    }
}