package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.aluno.*;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.dto.curso.CursoResumoDTO;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.service.CursoService;
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
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CursoService cursoService;

    public ResponseEntity<Map<String, Object>> cadastrarAluno(CadastroAluno dados) {
        Map<String, Object> response = new HashMap<>();

        if (usuarioRepository.existsByEmail(dados.email())) {
            throw new BadRequestException("Email já está em uso");
        }
        if (usuarioRepository.existsByCpf(dados.cpf())) {
            throw new BadRequestException("CPF já está cadastrado");
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
        Aluno alunoToUpdate = findByIdOrThrowsNotFoundException(dados.id());
        alunoToUpdate.setNome(dados.nome());
        alunoToUpdate.setEmail(dados.email());
        alunoToUpdate.setDataNascimento(dados.dataNascimento());
        alunoToUpdate.setAtivo(dados.ativo());
        alunoRepository.save(alunoToUpdate);

        return ResponseEntity.ok(Map.<String, Object>of("message", "Aluno atualizado com sucesso"));
    }

    public ResponseEntity<Map<String, Object>> removerAluno(long id) {
        alunoRepository.delete(findByIdOrThrowsNotFoundException(id));

        usuarioRepository.deleteById(id);

        return ResponseEntity.ok(Map.<String, Object>of("message", "Aluno removido com sucesso"));
    }
    public ResponseEntity<Map<String, Object>> obterAluno(long id) {
        Aluno aluno = findByIdOrThrowsNotFoundException(id);

        ObterAluno dto = new ObterAluno(aluno);
        return ResponseEntity.ok(Map.of("aluno", (Object) dto));
    }

    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);
    public Page<AlunoResumoDTO> listarAlunosResumo(Pageable paginacao) {
        logger.info("Listando alunos (resumo) com paginação: {}", paginacao);

        return alunoRepository.findAll(paginacao)
                .map(AlunoResumoDTO::new);
    }
    public List<AlunoResumoDTO> listarPorDataCadastro(LocalDate dataInicio, LocalDate dataFim) {
        return alunoRepository.findByDataCadastroBetween(dataInicio, dataFim)
                .stream()
                .map(AlunoResumoDTO::new)
                .toList();
    }
    @Transactional
    public ResponseEntity<MatriculaResponseDTO> matricularAluno(MatricularAlunoRequest request) {
        Aluno aluno = findByIdOrThrowsNotFoundException(request.alunoId());

        List<Curso> cursos = request.cursoIds().stream()
                .map(cursoService::findByIdOrThrowsNotFoundException)
                .toList();

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
        List<Aluno> alunos = request.alunoIds().stream().map(this::findByIdOrThrowsNotFoundException).toList();

        Curso curso = cursoService.findByIdOrThrowsNotFoundException(request.cursoId());

        alunos.forEach(aluno -> aluno.getCursos().add(curso));
        alunoRepository.saveAll(alunos);

        return ResponseEntity.ok(Map.of("message", "Alunos matriculados com sucesso"));
    }
    @Transactional
    public ResponseEntity<List<AlunoResumoDTO>> listarAlunosPorCurso(Long cursoId) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(cursoId);

        List<AlunoResumoDTO> alunos = curso.getAlunos()
                .stream()
                .map(AlunoResumoDTO::new)
                .toList();

        return ResponseEntity.ok(alunos);
    }

    public Aluno findByIdOrThrowsNotFoundException(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aluno com id '%s' não encontrado".formatted(id)));
    }
}