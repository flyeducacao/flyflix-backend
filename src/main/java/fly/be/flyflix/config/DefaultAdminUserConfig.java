package fly.be.flyflix.config;

import fly.be.flyflix.auth.entity.Admin;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.auth.repository.AdminRepository;
import fly.be.flyflix.auth.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Configuration
public class DefaultAdminUserConfig implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    public DefaultAdminUserConfig(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            UsuarioService usuarioService
    ) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        final String adminEmail = "dev_educ@flyeducacao.org";
        final String adminCpf = "68669859432";

        System.out.println("Buscando admin no banco...");
        var adminOptional = adminRepository.findByEmail(adminEmail);
        System.out.println("Resultado: " + adminOptional);

        adminOptional.ifPresentOrElse(
                admin -> System.out.println("Admin já existe no banco"),
                () -> {
                    Admin admin = new Admin();
                    admin.setNome("Admin");
                    admin.setEmail(adminEmail);
                    admin.setCpf(adminCpf);
                    admin.setSenha(passwordEncoder.encode("FlyAdmin*8"));
                    admin.setAtivo(true);
                    admin.setRole(Role.ADMIN);

                    usuarioService.adicionarFotoDePerfilPadrao(admin);

                    adminRepository.save(admin);
                    System.out.println("Admin padrão criado com sucesso");
                }
        );
    }
}
