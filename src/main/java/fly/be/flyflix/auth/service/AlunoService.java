package fly.be.flyflix.auth.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

    @Transactional
    public byte[] importarAlunosViaCsv(Long id, MultipartFile file) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(id);
        List<String[]> linhasComFeedback = new ArrayList<>();

        try(
                InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReader(reader)
        ) {
            List<String[]> linhas = csvReader.readAll();

            if (linhas.isEmpty()) {
                throw new BadRequestException("Arquivo CSV está vazio");
            }

            String[] linhaCabecalho = Arrays.copyOf(linhas.get(0), linhas.get(0).length + 1);
            linhaCabecalho[linhaCabecalho.length - 1] = "Feedback da importação";

            linhasComFeedback.add(linhaCabecalho);

            for (int i = 1; i < linhas.size(); i++) {
                String[] linha = linhas.get(i);
                String[] linhaComFeedback = Arrays.copyOf(linha, linha.length + 1);
                int posicaoColunaFeedback = linhaComFeedback.length - 1;

                String emailAluno = linha[1];

                if (emailAluno == null || emailAluno.isBlank()) {
                    linhaComFeedback[posicaoColunaFeedback] = "Email ausente";

                    linhasComFeedback.add(linhaComFeedback);
                    continue;
                }

                Optional<Aluno> optionalAluno = alunoRepository.findByEmail(emailAluno);

                if (optionalAluno.isEmpty()) {
                    linhaComFeedback[posicaoColunaFeedback] = "Aluno com email '%s' não cadastrado".formatted(emailAluno);

                    linhasComFeedback.add(linhaComFeedback);
                    continue;
                }

                Aluno alunoEncontrado = optionalAluno.get();

                boolean alunoJaMatriculado = alunoEncontrado.getCursos().stream().anyMatch(alunoCurso -> alunoCurso.getCurso().equals(curso));
                if (alunoJaMatriculado) {
                    linhaComFeedback[posicaoColunaFeedback] = "Aluno já está matriculado no curso";

                    linhasComFeedback.add(linhaComFeedback);
                    continue;
                }

                AlunoCurso alunoCurso = new AlunoCurso(AlunoCursoKey.by(alunoEncontrado, curso), alunoEncontrado, curso);

                alunoCursoService.save(alunoCurso);
                linhaComFeedback[posicaoColunaFeedback] = "Aluno cadastrado com sucesso";

                linhasComFeedback.add(linhaComFeedback);
            }
        } catch (CsvException | IOException e) {
            throw new UnprocessableEntityException("Não foi possível ler o arquivo");
        }

        try (ByteArrayOutputStream resposta = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(resposta, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            csvWriter.writeAll(linhasComFeedback);
            csvWriter.flush();

            return resposta.toByteArray();

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}