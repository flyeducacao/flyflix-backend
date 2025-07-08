package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.admin.AtualizarAdminRequest;
import fly.be.flyflix.auth.controller.dto.admin.CadastroAdmin;
import fly.be.flyflix.auth.controller.dto.admin.DadosAdminResponse;
import fly.be.flyflix.auth.entity.Admin;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.auth.repository.AdminRepository;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.auth.util.CpfValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UsuarioService usuarioService;

    public void cadastrarAdmin(CadastroAdmin dados) {
        usuarioService.assertEmailIsNotRegistered(dados.email());
        usuarioService.assertCpfDoesNotBelongsToAnotherUser(dados.cpf());
        CpfValidator.validarCpf(dados.cpf());


        Admin admin = new Admin();
        admin.setNome(dados.nome());
        admin.setEmail(dados.email());
        admin.setCpf(dados.cpf());
        admin.setAtivo(true);
        admin.setRole(Role.ADMIN);

        String senhaTemp = UUID.randomUUID().toString().substring(0, 8);
        admin.setSenha(passwordEncoder.encode(senhaTemp));

        usuarioService.adicionarFotoDePerfilPadrao(admin);
        adminRepository.save(admin);

        String corpo = String.format(
                "Olá %s,\n\n" +
                        "Sua conta foi criada com sucesso!\n" +
                        "Senha temporária: %s\n\n" +
                        "Por favor, altere sua senha após o primeiro login.\n\n" +
                        "Equipe FlyFlix",
                admin.getNome(),
                senhaTemp
        );

        emailService.enviarEmail(
                admin.getEmail(),
                "Cadastro de Administrador FlyFlix",
                corpo
        );
    }

    public void atualizarAdmin(AtualizarAdminRequest dados) {
        Admin admin = findByIdAndAtivoIsTrueOrThrowsNotFoundException(dados.id());

        usuarioService.assertEmailIsNotRegistered(dados.email(), admin);
        usuarioService.assertCpfDoesNotBelongsToAnotherUser(dados.cpf(), admin);
        CpfValidator.validarCpf(dados.cpf());

        admin.setNome(dados.nome());
        admin.setEmail(dados.email());
        //admin.setDataNascimento(dados.dataNascimento());
        admin.setCpf(dados.cpf());
        adminRepository.save(admin);
    }

    public void removerAdmin(Long id) {
        Admin adminToDesative = findByIdAndAtivoIsTrueOrThrowsNotFoundException(id);

        adminToDesative.setAtivo(false);
        adminRepository.save(adminToDesative);
    }


    public DadosAdminResponse obterAdmin(Long id) {
        Admin admin = findByIdOrThrowsNotFoundException(id);

        return DadosAdminResponse.by(admin);
    }


    public Page<Admin> listarAdmins(Pageable paginacao) {
        return adminRepository.findAllByAtivoIsTrue(paginacao);
    }

    public Admin findByIdAndAtivoIsTrueOrThrowsNotFoundException(Long id) {
        return adminRepository.findByIdAndAtivoIsTrue(id)
                .orElseThrow(() -> adminIdNotFound(id));
    }

    public Admin findByIdOrThrowsNotFoundException(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> adminIdNotFound(id));
    }

    public NotFoundException adminIdNotFound(Long id) {
        return new NotFoundException("Admin com id '%s' não encontrado".formatted(id));
    }
}
