package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.senha.AtualizarSenhaDTO;
import fly.be.flyflix.auth.controller.dto.senha.RedefinicaoSenhaDTO;
import fly.be.flyflix.auth.entity.Usuario;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public SenhaService(UsuarioRepository usuarioRepository,
                        TokenService tokenService,
                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public void atualizarSenha(AtualizarSenhaDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BadRequestException("Usuário não encontrado."));

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new BadRequestException("Senha atual incorreta.");
        }

        validarNovaSenha(dto.novaSenha());

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    public void redefinirSenha(RedefinicaoSenhaDTO dto) {
        validarNovaSenha(dto.novaSenha());

        Usuario usuario = tokenService.validarTokenRedefinicaoSenha(dto.token());
        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
        tokenService.invalidarToken(dto.token()); // opcional
    }

    private void validarNovaSenha(String senha) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=<>?{}\\[\\]~]).{8,}$";
        if (senha == null || !senha.matches(regex)) {
            throw new BadRequestException("A senha deve ter no mínimo 8 caracteres, uma letra maiúscula, um número e um caractere especial.");
        }
    }
}
