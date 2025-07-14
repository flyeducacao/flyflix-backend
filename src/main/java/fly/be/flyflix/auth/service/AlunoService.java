package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.aluno.*;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.entity.AlunoCurso;
import fly.be.flyflix.auth.entity.AlunoCursoKey;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.auth.exception.UnprocessableEntityException;
import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.conteudo.dto.curso.CursoResumoDTO;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.service.CursoService;
import fly.be.flyflix.auth.util.CpfValidator;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private AlunoCursoService alunoCursoService;

    public void cadastrarAluno(CadastroAluno dados) {
        usuarioService.assertEmailIsNotRegistered(dados.email());
        usuarioService.assertCpfDoesNotBelongsToAnotherUser(dados.cpf());
        CpfValidator.validarCpf(dados.cpf());

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

        usuarioService.adicionarFotoDePerfilPadrao(aluno);
        alunoRepository.save(aluno);

        String urlLogin = "https://flyeducacao.org";
        String assunto = "Sua conta FlyFlix está pronta!";
        String corpo = String.format(
                "<p>Oi, %s!</p>" +
                        "<p>Sua conta FlyFlix já está no sistema — 100%% conectada! 💻<br>" +
                        "Sua senha temporária de acesso é: <strong>%s</strong></p>" +
                        "<p>Recomendo trocar assim que logar, pra manter tudo seguro e sob controle.</p>" +
                        "<p>Agora é só clicar <a href=\"%s\">aqui</a> para dar login e curtir essa nova fase com a gente!<br>" +
                        "Se der qualquer bug, chama a gente rapidinho! 😉</p>" +
                        "<p>Abraço digital,<br>" +
                        "Equipe Fly 🤖</p>",
                dados.nome(),
                senhaTemp,
                urlLogin
        );

        emailService.enviarEmail(dados.email(), assunto, corpo);
    }

    public void atualizarAluno(AtualizarAlunoRequest dados) {
        Aluno alunoToUpdate = findByIdAndAtivoIsTrueOrThrowsNotFoundException(dados.id());

        usuarioService.assertEmailIsNotRegistered(dados.email(), alunoToUpdate);
        usuarioService.assertCpfDoesNotBelongsToAnotherUser(dados.cpf(), alunoToUpdate);
        CpfValidator.validarCpf(dados.cpf());

        LocalDate dataNascimento = dados.dataNascimento();
        assertDataNascimentoValida(dataNascimento);

        alunoToUpdate.setNome(dados.nome());
        alunoToUpdate.setEmail(dados.email());
        alunoToUpdate.setDataNascimento(dataNascimento);
        alunoToUpdate.setCpf(dados.cpf());
        alunoRepository.save(alunoToUpdate);
    }

    public void removerAluno(long id) {
        Aluno alunoToDesative = findByIdAndAtivoIsTrueOrThrowsNotFoundException(id);

        alunoToDesative.setAtivo(false);

        usuarioRepository.save(alunoToDesative);
    }

    public ObterAluno obterAluno(long id) {
        Aluno aluno = findByIdOrThrowsNotFoundException(id);

        return new ObterAluno(aluno);
    }

    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);
    public Page<AlunoResumoDTO> listarAlunosResumo(Pageable paginacao) {
        logger.info("Listando alunos (resumo) com paginação: {}", paginacao);

        return alunoRepository.findAllByAtivoIsTrue(paginacao)
                .map(AlunoResumoDTO::new);
    }

    public List<AlunoResumoDTO> listarPorDataCadastro(LocalDate dataInicio, LocalDate dataFim) {
        return alunoRepository.findByDataCadastroBetweenAndAtivoIsTrue(dataInicio, dataFim)
                .stream()
                .map(AlunoResumoDTO::new)
                .toList();
    }

    @Transactional
    public MatriculaResponseDTO matricularAluno(MatricularAlunoRequest request) {
        Aluno aluno = findByIdOrThrowsNotFoundException(request.alunoId());

        if (!aluno.getAtivo()) aluno.setAtivo(true);

        List<Curso> cursos = request.cursoIds().stream()
                .map(cursoService::findByIdOrThrowsNotFoundException)
                .toList();

        Set<AlunoCurso> newAlunoCursos = cursos.stream().map(curso -> {
            AlunoCursoKey alunoCursoId = AlunoCursoKey.by(aluno, curso);
            AlunoCurso alunoCurso = AlunoCurso.builder().id(alunoCursoId).aluno(aluno).curso(curso).build();

            return alunoCursoService.save(alunoCurso);
        }).collect(Collectors.toSet());

        List<CursoResumoDTO> cursosResumo = newAlunoCursos
                .stream()
                .map(alunoCurso -> new CursoResumoDTO(alunoCurso.getCurso().getId(), alunoCurso.getCurso().getTitulo()))
                .toList();

        MatriculaResponseDTO response = new MatriculaResponseDTO(
                aluno.getId(),
                aluno.getNome(),
                cursosResumo
        );

        return response;
    }

    @Transactional
    public void matricularAlunosEmLote(MatriculaEmLoteRequest request) {
        List<Aluno> alunos = request.alunoIds().stream()
                .map(id -> {
                    Aluno aluno = findByIdOrThrowsNotFoundException(id);
                    if (!aluno.getAtivo()) aluno.setAtivo(true);

                    return aluno;
                }).toList();

        Curso curso = cursoService.findByIdOrThrowsNotFoundException(request.cursoId());

        alunos.forEach(aluno -> {
            AlunoCursoKey alunoCursoId = AlunoCursoKey.by(aluno, curso);
            AlunoCurso alunoCurso = AlunoCurso.builder().id(alunoCursoId).curso(curso).aluno(aluno).build();

            AlunoCurso savedAlunoCurso = alunoCursoService.save(alunoCurso);

            aluno.getCursos().add(savedAlunoCurso);
        });
    }

    @Transactional
    public List<AlunoResumoDTO> listarAlunosPorCurso(Long cursoId) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(cursoId);

        return curso.getAlunos()
                .stream()
                .map(AlunoCurso::getAluno)
                .filter(Aluno::getAtivo)
                .map(AlunoResumoDTO::new)
                .toList();
    }

    public void importarAlunosViaXlsx(Long cursoId, MultipartFile file) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(cursoId);

        List<Aluno> alunosParaMatricula = lerPlanilhaImportAlunos(file);

        alunosParaMatricula.stream()
                .filter(aluno -> !aluno.getAtivo())
                .forEach(aluno -> aluno.setAtivo(true));

        alunosParaMatricula.forEach(aluno -> {
            AlunoCursoKey alunoCursoId = AlunoCursoKey.by(aluno, curso);
            AlunoCurso alunoCurso = AlunoCurso.builder().id(alunoCursoId).curso(curso).aluno(aluno).build();

            alunoCursoService.save(alunoCurso);
        });
    }

    private List<Aluno> lerPlanilhaImportAlunos(MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException("Arquivo não encontrado");

        List<Aluno> alunosParaMatricula = new ArrayList<>();

        try(
                InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                for (Cell cell : row) {
                    if (cell.getColumnIndex() != 1) continue;

                    String email = cell.getStringCellValue().trim();
                    if (email.isEmpty()) continue;

                    Aluno aluno = findByEmailOrThrowsNotFoundException(email);

                    alunosParaMatricula.add(aluno);
                }
            }
        } catch (IOException e) {
            throw new UnprocessableEntityException("Não foi possível carregar o arquivo");
        }

        return alunosParaMatricula;
    }

    public Aluno findByEmailOrThrowsNotFoundException(String email) {
        return alunoRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Aluno com email '%s' não encontrado".formatted(email)));
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