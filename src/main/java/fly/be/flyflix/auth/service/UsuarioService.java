package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.AdicionarFotoDePerfilDto;
import fly.be.flyflix.auth.controller.dto.AlteraFotoPerfilDto;
import fly.be.flyflix.auth.controller.dto.GetFotoPerfilDto;
import fly.be.flyflix.auth.controller.dto.UsuarioByGetMe;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ================================
    // FOTO DE PERFIL (apenas URL)
    // ================================

    public void salvarUrlFoto(Long id, AdicionarFotoDePerfilDto request) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);
        usuario.setFotoPerfilUrl(request.url());

        usuarioRepository.save(usuario);
    }

    public GetFotoPerfilDto obterUrlFoto(Long id) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);

        String response = usuario.getFotoPerfilUrl();
        return new GetFotoPerfilDto(response);
    }

    public void removerFoto(Long id) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);
        adicionarFotoDePerfilPadrao(usuario);
        usuarioRepository.save(usuario);
    }

    // ================================
    // DADOS DO USUÁRIO
    // ================================
    @Transactional
    public UsuarioByGetMe getMe(Long usuarioId) {
        Usuario usuario = findByIdOrThrowsNotFoundException(usuarioId);

        // Retorna a URL real se houver, senão o campo virá como null
        return UsuarioByGetMe.by(usuario);
    }


    public Usuario findByIdOrThrowsNotFoundException(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário com id '%s' não encontrado".formatted(id)));
    }

    public void adicionarFotoDePerfilPadrao(Usuario usuario) {
        String urlFotoDePerfilPadrao = "https://firebasestorage.googleapis.com/v0/b/flyeducation-1eea5.firebasestorage.app/o/fotoUsuario.jpg?alt=media&token=85ad7339-51d8-42ae-a392-b5b362cc7f15";

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

    public void atualizarFoto(Long id, AlteraFotoPerfilDto request) {
        Usuario usuario = findByIdOrThrowsNotFoundException(id);

        usuario.setFotoPerfilUrl(request.url());

        usuarioRepository.save(usuario);
    }
}