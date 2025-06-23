package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.admin.AtualizarAdminRequest;
import fly.be.flyflix.auth.controller.dto.admin.CadastroAdmin;
import fly.be.flyflix.auth.controller.dto.admin.DadosAdminResponse;
import fly.be.flyflix.auth.entity.Admin;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.auth.repository.AdminRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public ResponseEntity<Map<String, Object>> cadastrarAdmin(CadastroAdmin dados) {
        if (usuarioRepository.existsByEmail(dados.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email já está em uso"));
        }
        if (usuarioRepository.existsByCpf(dados.cpf())) {
            return ResponseEntity.badRequest().body(Map.of("error", "CPF já está cadastrado"));
        }

        Admin admin = new Admin();
        admin.setNome(dados.nome());
        admin.setEmail(dados.email());
        admin.setCpf(dados.cpf());
        admin.setAtivo(true);
        admin.setRole(Role.ADMIN);

        String senhaTemp = UUID.randomUUID().toString().substring(0, 8);
        admin.setSenha(passwordEncoder.encode(senhaTemp));

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
        String assunto = "Sua conta FlyFlix está pronta!";


        emailService.enviarEmail(
                admin.getEmail(),
                "Cadastro de Administrador FlyFlix",
                corpo
        );


        return ResponseEntity.ok(Map.of("message", "Administrador cadastrado com sucesso"));
    }

    public ResponseEntity<Map<String, String>> atualizarAdmin(AtualizarAdminRequest dados) {
        return adminRepository.findById(dados.id())
                .map(admin -> {
                    admin.setNome(dados.nome());
                    admin.setEmail(dados.email());
                    //admin.setDataNascimento(dados.dataNascimento());
                    admin.setAtivo(dados.ativo());
                    adminRepository.save(admin);
                    return ResponseEntity.ok(Map.of("message", "Administrador atualizado com sucesso"));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Administrador não encontrado")));
    }




    public ResponseEntity<Map<String, Object>> removerAdmin(Long id) {
        return adminRepository.findById(id)
                .map(admin -> {
                    adminRepository.delete(admin);
                    usuarioRepository.deleteById(admin.getId());
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Administrador removido com sucesso");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Administrador não encontrado");
                    return ResponseEntity.badRequest().body(error);
                });
    }


    public ResponseEntity<Map<String, Object>> obterAdmin(Long id) {
        return adminRepository.findById(id)
                .map(admin -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("admin", new DadosAdminResponse(
                            admin.getId(),
                            admin.getNome(),
                            admin.getEmail(),
                            admin.getCpf(),
                            // admin.getDataNascimento(), // Descomente se necessário
                            admin.getAtivo()
                    ));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.badRequest()
                        .body(Map.of("error", "Administrador não encontrado")));
    }


    public Page<Admin> listarAdmins(Pageable paginacao) {
        return adminRepository.findAll(paginacao);
    }
}
