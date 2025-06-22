package fly.be.flyflix.conteudo.controller;

import fly.be.flyflix.conteudo.dto.curso.AtualizacaoCurso;
import fly.be.flyflix.conteudo.dto.curso.CadastroCurso;
import fly.be.flyflix.conteudo.dto.curso.DetalhamentoCurso;
import fly.be.flyflix.conteudo.dto.modulo.ModuloByListarPorCurso;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.CursoModulo;
import fly.be.flyflix.conteudo.entity.Modulo;
import fly.be.flyflix.conteudo.repository.CursoModuloRepository;
import fly.be.flyflix.conteudo.repository.CursoRepository;
import fly.be.flyflix.conteudo.repository.ModuloRepository;
import fly.be.flyflix.conteudo.service.CursoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        Optional<Curso> optional = cursoRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Curso curso = optional.get();

        return ResponseEntity.ok(DetalhamentoCurso.by(curso));
    }



    @PutMapping("/{id}")
    public ResponseEntity<DetalhamentoCurso> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoCurso dados) {
        Optional<Curso> optional = cursoRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        try {
            Curso curso = optional.get();
            curso.setTitulo(dados.titulo());
            //curso.setDescricao(dados.descricao());
            // curso.setImagemCapa(dados.imagemCapa());

            return ResponseEntity.ok(DetalhamentoCurso.by(curso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!cursoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cursoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cursoId}/modulos/{moduloId}")
    public ResponseEntity<?> adicionarModulo(@PathVariable Long cursoId, @PathVariable Long moduloId) {
        try {
            Curso cursoAtualizado = cursoService.adicionarModuloAoCurso(cursoId, moduloId);
            return ResponseEntity.ok(DetalhamentoCurso.by(cursoAtualizado));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado");
        }
    }

    @PutMapping("/{idCurso}/modulos/{idModulo}")
    @Transactional
    public ResponseEntity<Void> adicionarModuloAoCurso(
            @PathVariable Long idCurso,
            @PathVariable Long idModulo,
            @RequestParam(required = false) Integer ordem // ordem opcional, pode definir aqui
    ) {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado"));

        Modulo modulo = moduloRepository.findById(idModulo)
                .orElseThrow(() -> new EntityNotFoundException("Módulo não encontrado"));

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
        cursoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso com id '%s' não encontrado".formatted(id)));

        List<ModuloByListarPorCurso> resposta = moduloRepository.findByCursoId(id).stream()
                .map(ModuloByListarPorCurso::by)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{idCurso}/modulos/{idModulo}")
    @Transactional
    public ResponseEntity<String> removerModuloDoCurso(@PathVariable Long idCurso, @PathVariable Long idModulo) {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado"));

        Modulo modulo = moduloRepository.findById(idModulo)
                .orElseThrow(() -> new EntityNotFoundException("Módulo não encontrado"));

        Optional<CursoModulo> cursoModuloOpt = cursoModuloRepository.findByCursoAndModulo(curso, modulo);
        if (cursoModuloOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Associação entre curso e módulo não encontrada.");
        }

        CursoModulo cursoModulo = cursoModuloOpt.get();

        curso.getCursoModulos().remove(cursoModulo);
        modulo.getCursoModulos().remove(cursoModulo);

        cursoModuloRepository.delete(cursoModulo);

        return ResponseEntity.ok("Módulo removido com sucesso.");
    }



}