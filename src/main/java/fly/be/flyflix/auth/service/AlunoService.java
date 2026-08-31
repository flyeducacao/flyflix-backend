package fly.be.flyflix.auth.service;

import fly.be.flyflix.auth.controller.dto.aluno.*;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.auth.enums.Role;
import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.auth.repository.UsuarioRepository;
import fly.be.flyflix.auth.util.CpfValidator;
import fly.be.flyflix.conteudo.dto.curso.CursoResumoDTO;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.service.CursoService;
import jakarta.transaction.Transactional;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AlunoService {
    private static final Logger log = LoggerFactory.getLogger(AlunoService.class);

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

    String urlLogin = "https://flyeducacao.org";
    public void cadastrarAluno(CadastroAluno dados) {
        log.info("Iniciando cadastro de aluno com email='{}' e cpf='{}'", dados.email(), dados.cpf());

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
        log.info("Aluno salvo com sucesso. id='{}', email='{}'", aluno.getId(), aluno.getEmail());


        //String urlLogin = frontendUrl + loginPath;

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

        try {
            emailService.enviarEmail(dados.email(), assunto, corpo);
            log.info("Email de boas-vindas enviado com sucesso para '{}'", dados.email());
        } catch (Exception e) {
            log.error("Aluno criado, mas falhou envio de email para '{}': {}", dados.email(), e.getMessage(), e);
            log.warn("Aluno cadastrado com sucesso, mas o email não foi enviado para '{}'", dados.email());
        }
    }

    public void assertDataNascimentoValida(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento não pode ser nula");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro");
        }
        if (dataNascimento.isBefore(LocalDate.of(1925, 1, 1))) {
            throw new IllegalArgumentException("Data de nascimento inválida");
        }
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

    public Aluno findByIdAndAtivoIsTrueOrThrowsNotFoundException(long id) {
        return alunoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Aluno ativo não encontrado com ID: " + id));
    }

    public ObterAluno obterAluno(long id) {
        Aluno aluno = findByIdOrThrowsNotFoundException(id);

        return new ObterAluno(aluno);
    }
    public Aluno findByIdOrThrowsNotFoundException(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Aluno não encontrado com ID: " + id));
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

        // adiciona os cursos à lista do aluno
        aluno.getCursos().addAll(cursos);
        alunoRepository.save(aluno);

        List<CursoResumoDTO> cursosResumo = cursos.stream()
                .map(curso -> new CursoResumoDTO(curso.getId(), curso.getTitulo()))
                .toList();

        return new MatriculaResponseDTO(
                aluno.getId(),
                aluno.getNome(),
                cursosResumo
        );
    }


    @Transactional
    public List<MatriculaEmLoteResponse> importarEMatricularAlunos(MultipartFile file, Long cursoId) {
        ultimosErros.clear(); // Limpa erros anteriores
        List<MatriculaEmLoteResponse> respostas = new ArrayList<>();
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(cursoId);

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // pula cabeçalho
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nome = getStringCellValue(row.getCell(0));
                String email = getStringCellValue(row.getCell(1));
                String cpf = getStringCellValue(row.getCell(2));
                LocalDate dataNascimento = getLocalDateCellValue(row.getCell(3));

                if (email == null || email.isBlank()) {
                    String motivo = "Email vazio";
                    respostas.add(new MatriculaEmLoteResponse(nome, email, "ERRO", motivo));
                    ultimosErros.add(new ErroImportacaoAlunoDTO(i + 1, nome, email, cpf, dataNascimento, motivo));
                    continue;
                }

                try {
                    Aluno aluno = alunoRepository.findByEmail(email).orElse(null);

                    if (aluno == null) {
                        // Validações e cadastro
                        usuarioService.assertEmailIsNotRegistered(email);
                        usuarioService.assertCpfDoesNotBelongsToAnotherUser(cpf);
                        CpfValidator.validarCpf(cpf);
                        assertDataNascimentoValida(dataNascimento);

                        aluno = new Aluno();
                        aluno.setNome(nome);
                        aluno.setEmail(email);
                        aluno.setCpf(cpf);
                        aluno.setDataNascimento(dataNascimento);
                        aluno.setAtivo(true);
                        aluno.setRole(Role.ALUNO);

                        String senhaTemp = SenhaGenerator.gerarSenhaTemporaria();
                        aluno.setSenha(passwordEncoder.encode(senhaTemp));
                        usuarioService.adicionarFotoDePerfilPadrao(aluno);

                        // Envia e-mail de boas-vindas

                        String assunto = "Sua conta FlyFlix está pronta!";
                        String corpo = String.format(
                                "<p>Oi, %s!</p>" +
                                        "<p>Sua conta FlyFlix já está no sistema! 💻<br>" +
                                        "Senha temporária: <strong>%s</strong></p>" +
                                        "<p>Clique <a href=\"%s\">aqui</a> para acessar!</p>" +
                                        "<p>Abraço digital,<br>Equipe Fly 🤖</p>",
                                nome, senhaTemp, urlLogin
                        );
                        emailService.enviarEmail(email, assunto, corpo);
                    }

                    // Matricula no curso
                    aluno.getCursos().add(curso); // Set garante unicidade
                    alunoRepository.save(aluno);

                    respostas.add(new MatriculaEmLoteResponse(aluno.getNome(), aluno.getEmail(), "SUCESSO"));

                } catch (Exception e) {
                    String motivo = e.getMessage();
                    respostas.add(new MatriculaEmLoteResponse(nome, email, "ERRO", motivo));
                    ultimosErros.add(new ErroImportacaoAlunoDTO(i + 1, nome, email, cpf, dataNascimento, motivo));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar planilha: " + e.getMessage(), e);
        }

        return respostas;
    }

    // Método para gerar relatório de erros
    public byte[] gerarRelatorioErros() {
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
            for (int i = 0; i < ultimosErros.size(); i++) {
                ErroImportacaoAlunoDTO erro = ultimosErros.get(i);
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

    // Auxiliares para leitura do Excel
    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK, _NONE, ERROR -> null;
        };
    }

    private LocalDate getLocalDateCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        return null;
    }



  private final List<ErroImportacaoAlunoDTO> ultimosErros = new ArrayList<>();


    public List<AlunoResumoDTO> listarAlunosPorCurso(Long cursoId) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(cursoId);

        List<Aluno> alunos = alunoRepository.findByCursosContaining(curso);

        return alunos.stream()
                .map(AlunoResumoDTO::new)
                .collect(Collectors.toList());
    }
}

