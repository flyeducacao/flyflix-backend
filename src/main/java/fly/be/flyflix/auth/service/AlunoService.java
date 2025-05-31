package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.CadastroAluno;
import fly.be.flyflix.auth.controller.dto.aluno.AtualizarAlunoRequest;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.enums.PerfilAluno;
import fly.be.flyflix.auth.repository.AlunoRepository;
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
public class AlunoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public ResponseEntity<Map<String, Object>> cadastrarAluno(CadastroAluno dados) {
        Map<String, Object> response = new HashMap<>();

        if (usuarioRepository.existsByEmail(dados.email())) {
            response.put("error", "Email já está em uso");
            return ResponseEntity.badRequest().body(response);
        }
        if (usuarioRepository.existsByCpf(dados.cpf())) {
            response.put("error", "CPF já está cadastrado");
            return ResponseEntity.badRequest().body(response);
        }

        Aluno aluno = new Aluno();
        aluno.setCpf(dados.cpf());
        aluno.setNome(dados.nome());
        aluno.setEmail(dados.email());
        aluno.setDataNascimento(dados.dataNascimento());
        aluno.setAtivo(true);
        // Define perfil padrão caso não venha do DTO
        aluno.setPerfilAluno(dados.perfilAluno() != null ? dados.perfilAluno() : PerfilAluno.MULHERES_IN_TECH);


        String senhaTemp = UUID.randomUUID().toString().substring(0, 8);
        aluno.setSenha(passwordEncoder.encode(senhaTemp));

        usuarioRepository.save(aluno);

        String assunto = "Sua senha temporária para FlyFlix";
        String corpo = String.format(
                "Olá %s,\n\n" +
                        "Sua conta foi criada com sucesso. Sua senha temporária é:\n\n" +
                        "%s\n\n" +
                        "Por favor, altere sua senha após o primeiro login.\n\n" +
                        "Atenciosamente,\nEquipe FlyFlix",
                dados.nome(),
                senhaTemp
        );

        emailService.enviarEmail(dados.email(), assunto, corpo);


        response.put("message", "Aluno cadastrado com sucesso. Senha enviada por email.");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> atualizarAluno(AtualizarAlunoRequest dados) {
        return alunoRepository.findById(dados.id())
                .map(aluno -> {
                    aluno.setNome(dados.nome());
                    aluno.setEmail(dados.email());
                    aluno.setDataNascimento(dados.dataNascimento());
                    aluno.setAtivo(dados.ativo());
                    alunoRepository.save(aluno);
                    return ResponseEntity.ok(Map.<String, Object>of("message", "Aluno atualizado com sucesso"));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        Map.of("error", "Aluno não encontrado")
                ));
    }



    public ResponseEntity<Map<String, Object>> removerAluno(long id) {
        return alunoRepository.findById(id)
                .map(aluno -> {
                    alunoRepository.delete(aluno);
                    usuarioRepository.deleteById(aluno.getId());
                    return ResponseEntity.ok(Map.<String, Object>of("message", "Aluno removido com sucesso"));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        Map.of("error", "Aluno não encontrado")
                ));
    }




    public ResponseEntity<Map<String, Object>> obterAluno(long id) {
        return alunoRepository.findById(id)
                .map(aluno -> ResponseEntity.ok(Map.<String, Object>of("aluno", aluno)))
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        Map.of("error", "Aluno não encontrado")
                ));
    }


    public Page<Aluno> listarAlunos(Pageable paginacao) {
        return alunoRepository.findAll(paginacao);
    }
}