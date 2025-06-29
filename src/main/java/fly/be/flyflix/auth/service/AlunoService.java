package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.aluno.*;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.entity.Usuario;
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
    @Autowired
    private UsuarioService usuarioService;

    public ResponseEntity<Map<String, Object>> cadastrarAluno(CadastroAluno dados) {
        Map<String, Object> response = new HashMap<>();

        usuarioService.assertEmailIsNotRegistered(dados.email());
        usuarioService.assertCpfDoesNotBelongsToAnotherUser(dados.cpf());

        LocalDate dataNascimento = dados.dataNascimento();
        assertDataNascimentoValida(dataNascimento);

        Aluno aluno = new Aluno();
        aluno.setCpf(dados.cpf());
        aluno.setNome(dados.nome());
        aluno.setEmail(dados.email());
        aluno.setDataNascimento(dataNascimento);
        aluno.setAtivo(true);
        aluno.setRole(Role.ALUNO);

        String senhaTemp = SenhaGenerator.gerarSenhaTemporaria();
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
        Aluno alunoToUpdate = findByIdAndAtivoIsTrueOrThrowsNotFoundException(dados.id());

        usuarioService.assertEmailIsNotRegistered(dados.email(), alunoToUpdate);
        usuarioService.assertCpfDoesNotBelongsToAnotherUser(dados.cpf(), alunoToUpdate);

        LocalDate dataNascimento = dados.dataNascimento();
        assertDataNascimentoValida(dataNascimento);

        alunoToUpdate.setNome(dados.nome());
        alunoToUpdate.setEmail(dados.email());
        alunoToUpdate.setDataNascimento(dataNascimento);
        alunoToUpdate.setCpf(dados.cpf());
        alunoRepository.save(alunoToUpdate);

        return ResponseEntity.ok(Map.of("message", "Aluno atualizado com sucesso"));
    }

    public ResponseEntity<Map<String, Object>> removerAluno(long id) {
        Aluno alunoToDesative = findByIdAndAtivoIsTrueOrThrowsNotFoundException(id);

        alunoToDesative.setAtivo(false);

        usuarioRepository.save(alunoToDesative);

        return ResponseEntity.ok(Map.of("message", "Aluno removido com sucesso"));
    }
    public ResponseEntity<Map<String, Object>> obterAluno(long id) {
        Aluno aluno = findByIdOrThrowsNotFoundException(id);

        ObterAluno dto = new ObterAluno(aluno);
        return ResponseEntity.ok(Map.of("aluno", dto));
    }

    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);
    public Page<AlunoResumoDTO> listarAlunosResumo(Pageable paginacao) {
        logger.info("Listando alunos (resumo) com paginação: {}", paginacao);

        return alunoRepository.findAllByAtivoIsTrue(paginacao)
                .map(aluno -> new AlunoResumoDTO(aluno, gerarUrlFotoUsuario(aluno)));
    }

    private String gerarUrlFotoUsuario(Usuario usuario) {
        return "/usuarios/%s/foto".formatted(usuario.getId());
    }

    public List<AlunoResumoDTO> listarPorDataCadastro(LocalDate dataInicio, LocalDate dataFim) {
        return alunoRepository.findByDataCadastroBetweenAndAtivoIsTrue(dataInicio, dataFim)
                .stream()
                .map(aluno -> new AlunoResumoDTO(aluno, gerarUrlFotoUsuario(aluno)))
                .toList();
    }
    @Transactional
    public ResponseEntity<MatriculaResponseDTO> matricularAluno(MatricularAlunoRequest request) {
        Aluno aluno = findByIdOrThrowsNotFoundException(request.alunoId());

        if (!aluno.getAtivo()) aluno.setAtivo(true);

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
        List<Aluno> alunos = request.alunoIds().stream()
                .map(id -> {
                    Aluno aluno = findByIdOrThrowsNotFoundException(id);
                    if (!aluno.getAtivo()) aluno.setAtivo(true);

                    return aluno;
                }).toList();

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
                .filter(Aluno::getAtivo)
                .map(aluno -> new AlunoResumoDTO(aluno, gerarUrlFotoUsuario(aluno)))
                .toList();

        return ResponseEntity.ok(alunos);
    }

    public Aluno findByIdAndAtivoIsTrueOrThrowsNotFoundException(Long id) {
        return alunoRepository.findByIdAndAtivoIsTrue(id)
                .orElseThrow(() -> alunoIdNotFound(id));
    }

    public Aluno findByIdOrThrowsNotFoundException(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> alunoIdNotFound(id));
    }

    public NotFoundException alunoIdNotFound(Long id) {
        return new NotFoundException("Aluno com id '%s' não encontrado".formatted(id));
    }

    public void assertDataNascimentoValida(LocalDate dataNascimento) {
        boolean isLessThan100YearsOld = dataNascimento.isAfter(LocalDate.now().minusYears(100));
        boolean isAtLeast10YearsOld = dataNascimento.isBefore(LocalDate.now().minusYears(10));

        boolean isDataValida = isAtLeast10YearsOld && isLessThan100YearsOld;

        if(!isDataValida) throwsDataNascimentoInvalida();
    }

    private void throwsDataNascimentoInvalida() {
        LocalDate cemAnosAtras = LocalDate.now().minusYears(100);
        LocalDate dezAnosAtras = LocalDate.now().minusYears(10);

        throw new BadRequestException("Data nascimento deve ser entre %s e %s".formatted(cemAnosAtras, dezAnosAtras));
    }
}