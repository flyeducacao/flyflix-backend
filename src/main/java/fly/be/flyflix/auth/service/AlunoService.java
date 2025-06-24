package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.aluno.*;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.dto.curso.CursoResumoDTO;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.repository.CursoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class AlunoService {
    @Autowired
    private CursoRepository cursoRepository;

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
        aluno.setRole(Role.ALUNO);

        String senhaTemp = UUID.randomUUID().toString().substring(0, 8);
        aluno.setSenha(passwordEncoder.encode(senhaTemp));

        alunoRepository.save(aluno);

        String assunto = "Sua conta FlyFlix está pronta!";
        String corpo = String.format(
                "<p>Oi, %s!</p>" +
                        "<p>Sua conta FlyFlix já está no sistema — 100%% conectada! 💻<br>" +
                        "Sua senha temporária de acesso é: <strong>%s</strong></p>" +
                        "<p>Recomendo trocar assim que logar, pra manter tudo seguro e sob controle.</p>" +
                        "<p>Agora é só clicar aqui para dar login e curtir essa nova fase com a gente!<br>" +
                        "Se der qualquer bug, chama a gente rapidinho! 😉</p>" +
                        "<p>Abraço digital,<br>" +
                        "Equipe Fly 🤖</p>",
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
                        Map.of("erro", "Aluno não encontrado")
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
                        Map.of("erro", "Aluno não encontrado")
                ));
    }
    public ResponseEntity<Map<String, Object>> obterAluno(long id) {
        return alunoRepository.findById(id)
                .map(aluno -> {
                    ObterAluno dto = new ObterAluno(aluno);
                    return ResponseEntity.ok(Map.of("aluno", (Object) dto));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        Map.of("erro", "Aluno não encontrado")
                ));
    }

    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);
    public Page<AlunoResumoDTO> listarAlunosResumo(Pageable paginacao) {
        try {
            logger.info("Listando alunos (resumo) com paginação: {}", paginacao);
            return alunoRepository.findAll(paginacao)
                    .map(AlunoResumoDTO::new);
        } catch (Exception e) {
            logger.error("Erro ao listar alunos", e);
            throw e;
        }
    }
    public List<AlunoResumoDTO> listarPorDataCadastro(LocalDate dataInicio, LocalDate dataFim) {
        return alunoRepository.findByDataCadastroBetween(dataInicio, dataFim)
                .stream()
                .map(AlunoResumoDTO::new)
                .toList();
    }
    @Transactional
    public ResponseEntity<MatriculaResponseDTO> matricularAluno(MatricularAlunoRequest request) {
        Optional<Aluno> alunoOpt = alunoRepository.findById(request.alunoId());
        if (alunoOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Aluno aluno = alunoOpt.get();

        List<Curso> cursos = cursoRepository.findAllById(request.cursoIds());

        if (cursos.size() != request.cursoIds().size()) {
            return ResponseEntity.badRequest().build();
        }

        aluno.getCursos().addAll(cursos);
        alunoRepository.save(aluno);

        List<CursoResumoDTO> cursosResumo = aluno.getCursos()
                .stream()
                .map(curso -> new CursoResumoDTO(curso.getId(), curso.getTitulo()))
                .toList();

        MatriculaResponseDTO response = new MatriculaResponseDTO(
                aluno.getId(),
                aluno.getNome(),
                cursosResumo
        );

        return ResponseEntity.ok(response);
    }
    @Transactional
    public ResponseEntity<?> matricularAlunosEmLote(MatriculaEmLoteRequest request) {
        List<Aluno> alunos = alunoRepository.findAllById(request.alunoIds());
        Optional<Curso> cursoOpt = cursoRepository.findById(request.cursoId());

        if (cursoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Curso não encontrado"));
        }

        if (alunos.size() != request.alunoIds().size()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Algum aluno não foi encontrado"));
        }

        Curso curso = cursoOpt.get();
        alunos.forEach(aluno -> aluno.getCursos().add(curso));
        alunoRepository.saveAll(alunos);

        return ResponseEntity.ok(Map.of("message", "Alunos matriculados com sucesso"));
    }
    @Transactional
    public ResponseEntity<List<AlunoResumoDTO>> listarAlunosPorCurso(Long cursoId) {
        Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);

        if (cursoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        List<AlunoResumoDTO> alunos = cursoOpt.get().getAlunos()
                .stream()
                .map(AlunoResumoDTO::new)
                .toList();

        return ResponseEntity.ok(alunos);
    }
}