package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.dto.curso.DetalhamentoCurso;
import fly.be.flyflix.conteudo.dto.modulo.ModuloByListarPorCurso;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import fly.be.flyflix.conteudo.repository.CursoModuloRepository;
import fly.be.flyflix.conteudo.repository.CursoRepository;
import fly.be.flyflix.conteudo.repository.ModuloRepository;
import fly.be.flyflix.conteudo.service.CursoService;
import fly.be.flyflix.conteudo.service.ModuloService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {
    @Autowired
    private CursoModuloRepository cursoModuloRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private ModuloRepository moduloRepository;
    @Autowired
    private CursoService cursoService;
    @Autowired
    private ModuloService moduloService;
    // endpoint para cadastro de cursos novos

    @PostMapping
    public ResponseEntity<DetalhamentoCurso> cadastrar(@RequestBody @Valid CadastroCurso dados) {
        try {
            Curso curso = cursoService.cadastrarCurso(dados);
            return ResponseEntity
                    .created(URI.create("/api/cursos/" + curso.getId()))
                    .body(DetalhamentoCurso.by(curso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public Page<DetalhamentoCurso> listar(@PageableDefault(size = 10, sort = "titulo") Pageable paginacao) {
        return cursoRepository.findAll(paginacao).map(DetalhamentoCurso::by);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<DetalhamentoCurso> detalhar(@PathVariable Long id) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(id);

        return ResponseEntity.ok(DetalhamentoCurso.by(curso));
    }



    @PutMapping("/{id}")
    public ResponseEntity<DetalhamentoCurso> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoCurso dados) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(id);
        curso.setTitulo(dados.titulo());

        return ResponseEntity.ok(DetalhamentoCurso.by(curso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cursoRepository.delete(cursoService.findByIdOrThrowsNotFoundException(id));

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cursoId}/modulos/{moduloId}")
    public ResponseEntity<?> adicionarModulo(@PathVariable Long cursoId, @PathVariable Long moduloId) {
        Curso cursoAtualizado = cursoService.adicionarModuloAoCurso(cursoId, moduloId);

        return ResponseEntity.ok(DetalhamentoCurso.by(cursoAtualizado));
    }

    @PutMapping("/{idCurso}/modulos/{idModulo}")
    @Transactional
    public ResponseEntity<Void> adicionarModuloAoCurso(
            @PathVariable Long idCurso,
            @PathVariable Long idModulo,
            @RequestParam(required = false) Integer ordem // ordem opcional, pode definir aqui
    ) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(idCurso);

        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(idModulo);

        // Verificar se já existe associação para evitar duplicidade
        boolean existe = cursoModuloRepository.existsByCursoAndModulo(curso, modulo);
        if (existe) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // ou outro tratamento
        }

        // Definir uma ordem padrão, se não informada
        if (ordem == null) {
            ordem = 1; // ou lógica para pegar última ordem + 1
        }

        CursoModulo cursoModulo = new CursoModulo(curso, modulo, ordem);
        cursoModuloRepository.save(cursoModulo);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/modulos")
    public ResponseEntity<List<ModuloByListarPorCurso>> listarModulosPorCurso(@PathVariable Long id) {
        cursoService.findByIdOrThrowsNotFoundException(id);

        List<ModuloByListarPorCurso> resposta = moduloRepository.findByCursoId(id).stream()
                .map(ModuloByListarPorCurso::by)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idCurso}/modulos/{idModulo}")
    @Transactional
    public ResponseEntity<String> removerModuloDoCurso(@PathVariable Long idCurso, @PathVariable Long idModulo) {
        Curso curso = cursoService.findByIdOrThrowsNotFoundException(idCurso);

        Modulo modulo = moduloService.findByIdOrThrowsNotFoundException(idModulo);

        Optional<CursoModulo> cursoModuloOpt = cursoModuloRepository.findByCursoAndModulo(curso, modulo);
        if (cursoModuloOpt.isEmpty()) {
            throw new NotFoundException("Associação entre curso e módulo não encontrada.");
        }

        CursoModulo cursoModulo = cursoModuloOpt.get();

        curso.getCursoModulos().remove(cursoModulo);
        modulo.getCursoModulos().remove(cursoModulo);

        cursoModuloRepository.delete(cursoModulo);

        return ResponseEntity.ok("Módulo removido com sucesso.");
    }
}