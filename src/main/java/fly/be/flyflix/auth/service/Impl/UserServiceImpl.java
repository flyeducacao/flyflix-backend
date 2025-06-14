//package fly.be.flyflix.auth.service.Impl;
//
//import fly.be.flyflix.auth.controller.dto.DadosAtualizacaoAluno;
//import fly.be.flyflix.auth.controller.dto.DadosDetalhamentoAluno;
//import fly.be.flyflix.auth.controller.dto.ObterAluno;
//import fly.be.flyflix.auth.entity.Aluno;
//import fly.be.flyflix.auth.controller.dto.CadastroAluno;
//import fly.be.flyflix.auth.enums.PerfilAluno;
//import fly.be.flyflix.auth.entity.PerfilUsuario;
//import fly.be.flyflix.auth.entity.Usuario;
//import fly.be.flyflix.auth.repository.AlunoRepository;
//import fly.be.flyflix.auth.repository.PerfilUsuarioRepository;
//import fly.be.flyflix.auth.repository.UsuarioRepository;
//import fly.be.flyflix.auth.service.UserService;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//
//@Service
//public class UserServiceImpl implements UserService {
//
//
//    private final UsuarioRepository usuarioRepository;
//    private final AlunoRepository alunoRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final PerfilUsuarioRepository perfilUsuarioRepository;
//
//    public UserServiceImpl(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository, PasswordEncoder passwordEncoder, PerfilUsuarioRepository perfilUsuarioRepository) {
//        this.usuarioRepository = usuarioRepository;
//        this.alunoRepository = alunoRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.perfilUsuarioRepository = perfilUsuarioRepository;
//    }
//
//    //CadastrarUsuario
//    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//    @Override
//    public ResponseEntity<Map<String, Object>> cadastrarAluno(CadastroAluno dados) {
//
//        //validar se o email ja esta cadastrado no banco
//        var usuarioEmailDB = usuarioRepository.findByLogin(dados.email());
//        if (usuarioEmailDB.isPresent()) {
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("campo", "email");
//            response.put("message", "Ja tem um cadastro com esse email");
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//        }
//
//        //validar se o cpf ja esta cadastrado no banco
//        var usuarioCpfDB = usuarioRepository.findByCpf(dados.cpf());
//        if (usuarioCpfDB.isPresent()) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("campo", "cpf");
//            response.put("message", "Ja tem um cadastro com esse CPF");
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//        }
//
//        //Criando objeto com os dados do usuario
//
//        //validando que perfil existe
//        PerfilUsuario.Values perfilUsuarioEnum = PerfilUsuario.Values.valueOf(dados.perfil().toUpperCase());
//        PerfilUsuario usuarioPerfil = perfilUsuarioRepository.findByName(perfilUsuarioEnum.name().toLowerCase());
//
//        // Verificar se o perfil foi encontrado
//        if (usuarioPerfil == null) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Dados do perfil:" + perfilUsuarioEnum.name() + " nao encontrados");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//
//        }
//
//        //Criando objeto com os dados do usuario
//        Usuario usuario = Usuario.builder()
//                .cpf(dados.cpf())
//                .login(dados.email())
//                .senha(passwordEncoder.encode(dados.senha()))
//                .perfiles(Set.of(usuarioPerfil))  // Asignando Rol de ADMIN OU ALUNO
//                .build();
//        usuarioRepository.save(usuario);
//
//
//        //Criando objeto com os dados do aluno
//        PerfilAluno perfilAlunoEnum = PerfilAluno.valueOf(dados.perfilAluno().replace(" ", "_").toUpperCase());
//        Aluno aluno = Aluno.builder()
//                    .cpf(dados.cpf())
//                    .email(dados.email())
//                    .usuario(usuario)
//                    .nome(dados.nome())
//                    .dataNascimento(dados.dataNascimento())
//                    .perfilAluno(perfilAlunoEnum)
//                    .ativo(true)
//                    .build();
//
//        alunoRepository.save(aluno);
//
//        return ResponseEntity.ok().build();
//    }
//
//    //AtualizarAluno
//    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//    @Override
//    public ResponseEntity<Map<String, Object>> atualizarAluno(DadosAtualizacaoAluno dados) {
//        var alunoDB = alunoRepository.getReferenceById(dados.id());
//        var usuarioDB = alunoDB.getUsuario();
//
//        PerfilAluno perfilAlunoEnum = PerfilAluno.valueOf(dados.perfilAluno().replace(" ", "_").toUpperCase());
//
//        //validar que tudos os dados estao preenchidos
//         if( dados.nome() != null && dados.email() != null && dados.cpf() != null && dados.dataNascimento() != null && dados.ativo() != null){
//             //setar dados do aluno
//            alunoDB.setNome(dados.nome());
//            alunoDB.setEmail(dados.email());
//            alunoDB.setCpf(dados.cpf());
//            alunoDB.setDataNascimento(dados.dataNascimento());
//            alunoDB.setPerfilAluno(perfilAlunoEnum);
//            alunoDB.setAtivo(dados.ativo());
//
//            //setar os dados do usuario do aluno email e cpf
//            usuarioDB.setLogin(dados.email());
//            usuarioDB.setCpf(dados.cpf());
//            //atualizar o aluno
//            alunoRepository.save(alunoDB);
//
//             Map<String, Object> response = new HashMap<>();
//             response.put("message", "Dados de Aluno atualizados com sucesso");
//
//             return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
//         }
//
//         //caso algum dado esteja vazio
//         Map<String, Object> response = new HashMap<>();
//         response.put("message", "Todos os dados devem ser preenchidos");
//
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//
//    }
//
//    //Remover Aluno
//    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//    @Override
//    public ResponseEntity<Map<String, Object>> removerAluno(long id) {
//        Aluno aluno = alunoRepository.getReferenceById(id);
//        if (aluno != null) {
//            aluno.inativar();
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Usuario removido com sucesso");
//
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        }
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Usuario não existe");
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    //Get Aluno
//    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//    @Override
//    public ResponseEntity<Map<String, Object>> obterAluno(long id) {
//        Optional<ObterAluno> alunoDB = alunoRepository.findById(id).map(ObterAluno::new);
//
//        if (alunoDB.isEmpty()) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Aluno não encontrado");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//
//        }
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Aluno encontrado");
//        response.put("data", alunoDB);
//
//        return ResponseEntity.status(HttpStatus.FOUND).body(response);
//    }
//
//    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//    @Override
//    public Page<DadosDetalhamentoAluno> listar(Pageable paginacao) {
//
//        return alunoRepository.findAllByAtivoTrue(paginacao)
//                .map(DadosDetalhamentoAluno::new);
//    }
//
//
//}
