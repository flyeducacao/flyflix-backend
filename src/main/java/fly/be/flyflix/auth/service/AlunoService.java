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
import fly.be.flyflix.auth.util.CpfValidator;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import jakarta.validation.Validator;

import java.io.ByteArrayOutputStream;
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
    private Validator validator;


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

        aluno.getCursos().addAll(cursos);
        alunoRepository.save(aluno);
        Set<AlunoCurso> newAlunoCursos = cursos.stream().map(curso -> {
            AlunoCurso alunoCurso = AlunoCurso.builder().id(alunoCursoId).aluno(aluno).curso(curso).build();

            return alunoCursoRepository.save(alunoCurso);
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

            AlunoCurso savedAlunoCurso = alunoCursoRepository.save(alunoCurso);

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
    public ResultadoImportacaoAlunosDTO importarAlunosViaPlanilha(MultipartFile file) {
        List<ErroImportacaoAlunoDTO> erros = new ArrayList<>();
        List<CadastroAluno> alunos = lerAlunosExcel(file, erros);

        int totalImportadosComSucesso = 0;
        int linha = 2;

        for (CadastroAluno dto : alunos) {
            Set<ConstraintViolation<CadastroAluno>> violacoes = validator.validate(dto);
            if (!violacoes.isEmpty()) {
                erros.add(new ErroImportacaoAlunoDTO(dto, linha, formatarErrosDeValidacao(violacoes)));
            } else {
                try {
                    cadastrarAluno(dto);
                    totalImportadosComSucesso++;
                } catch (Exception e) {
                    erros.add(new ErroImportacaoAlunoDTO(dto, linha, e.getMessage()));
                }
            }
            linha++;
        }
        this.ultimosErros = erros;

        return new ResultadoImportacaoAlunosDTO(totalImportadosComSucesso, erros);
    }


    private List<CadastroAluno> lerAlunosExcel(MultipartFile file, List<ErroImportacaoAlunoDTO> erros) {
        List<CadastroAluno> alunos = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int linha = i + 1; // Excel é 1-based

                try {
                    String nome = getValorString(row, 0);
                    String email = getValorString(row, 1);
                    String cpf = getValorString(row, 2);
                    LocalDate dataNascimento = getValorData(row, 3);

                    alunos.add(new CadastroAluno(nome, email, cpf, dataNascimento));
                } catch (Exception e) {
                    erros.add(new ErroImportacaoAlunoDTO(
                            linha,
                            getValorSeguro(row, 0),
                            getValorSeguro(row, 1),
                            getValorSeguro(row, 2),
                            null,
                            "Erro ao ler dados: " + e.getMessage()
                    ));
                }
            }

        } catch (Exception e) {
            erros.add(new ErroImportacaoAlunoDTO(0, "", "", "", null, "Erro ao abrir a planilha: " + e.getMessage()));
        }

        return alunos;
    }
    private String getValorString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) throw new IllegalArgumentException("Campo vazio na coluna " + (index + 1));
        return cell.getStringCellValue().trim();
    }

    private String getValorSeguro(Row row, int index) {
        try {
            Cell cell = row.getCell(index);
            return (cell != null) ? cell.toString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private LocalDate getValorData(Row row, int index) {
        Cell cell = row.getCell(index);
        if (!DateUtil.isCellDateFormatted(cell)) {
            throw new IllegalArgumentException("Data inválida na coluna " + (index + 1));
        }
        return cell.getLocalDateTimeCellValue().toLocalDate();
    }


    private String formatarErrosDeValidacao(Set<ConstraintViolation<CadastroAluno>> violacoes) {
        return violacoes.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
    }
    public byte[] gerarRelatorioErros() {

        List<ErroImportacaoAlunoDTO> erros = this.ultimosErros;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Erros de Importação");

            // Cabeçalho
            Row header = sheet.createRow(0);
            String[] colunas = {"Linha", "Nome", "Email", "CPF", "Data de Nascimento", "Erro"};
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(colunas[i]);
            }

            // Linhas de erro
            for (int i = 0; i < erros.size(); i++) {
                ErroImportacaoAlunoDTO erro = erros.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(erro.linha());
                row.createCell(1).setCellValue(erro.nome());
                row.createCell(2).setCellValue(erro.email());
                row.createCell(3).setCellValue(erro.cpf());
                row.createCell(4).setCellValue(erro.dataNascimento() != null ? erro.dataNascimento().toString() : "");
                row.createCell(5).setCellValue(erro.motivoErro());
            }

            // Ajustar largura automática das colunas
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar planilha de erros", e);
        }
    }
    private List<ErroImportacaoAlunoDTO> ultimosErros = new ArrayList<>();

    public List<ErroImportacaoAlunoDTO> getUltimosErrosImportacao() {
        return ultimosErros;
    }



}

