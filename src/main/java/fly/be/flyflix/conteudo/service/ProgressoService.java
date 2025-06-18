package fly.be.flyflix.conteudo.service;

import fly.be.flyflix.auth.repository.AlunoRepository;
import fly.be.flyflix.conteudo.dto.aula.AulaResumoDTO;
import fly.be.flyflix.conteudo.dto.progresso.ProgressoResponseDTO;
import fly.be.flyflix.conteudo.entity.Aula;
import fly.be.flyflix.auth.entity.Aluno;
import fly.be.flyflix.conteudo.entity.Curso;
import fly.be.flyflix.conteudo.entity.ProgressoAluno;
import fly.be.flyflix.conteudo.repository.AulaRepository;
import fly.be.flyflix.conteudo.repository.CursoRepository;
import fly.be.flyflix.conteudo.repository.ProgressoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressoService {

    private final AulaRepository aulaRepository;
    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;
    private final ProgressoRepository progressoRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProgressoService.class);

    @Autowired
    public ProgressoService(ProgressoRepository progressoRepository, AulaRepository aulaRepository, AlunoRepository alunoRepository, CursoRepository cursoRepository) {
        this.progressoRepository = progressoRepository;
        this.aulaRepository = aulaRepository;
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
    }

    public void marcarComoAssistida(Long alunoId, Long aulaId, Long cursoId) {
        Optional<ProgressoAluno> progressoExistente = progressoRepository.findByAluno_IdAndAula_Id(alunoId, aulaId);

        // Buscar as entidades relacionadas
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado: " + alunoId));
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new EntityNotFoundException("Aula não encontrada: " + aulaId));
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado: " + cursoId));

        if (progressoExistente.isPresent()) {
            ProgressoAluno progresso = progressoExistente.get();
            progresso.setAssistida(true);
            progresso.setAluno(aluno);   // atualiza referência para entidade Aluno
            progresso.setAula(aula);     // atualiza referência para entidade Aula
            progresso.setCurso(curso);   // atualiza referência para entidade Curso
            progressoRepository.save(progresso);
        } else {
            ProgressoAluno novo = new ProgressoAluno();
            novo.setAluno(aluno);
            novo.setAula(aula);
            novo.setCurso(curso);
            novo.setAssistida(true);
            progressoRepository.save(novo);
        }
    }

    public ProgressoResponseDTO obterProgresso(Long alunoId, Long cursoId) {
        try {
            long totalAulas = aulaRepository.countAulasByCursoId(cursoId);
            long aulasAssistidas = progressoRepository.countByAlunoIdAndCursoIdAndAssistidaTrue(alunoId, cursoId);
            double porcentagem = totalAulas == 0 ? 0.0 : (aulasAssistidas * 100.0) / totalAulas;

            return new ProgressoResponseDTO(
                    alunoId,
                    cursoId,
                    totalAulas,
                    aulasAssistidas,
                    porcentagem
            );
        } catch (Exception e) {
            logger.error("Erro ao calcular progresso do alunoId={} no cursoId={}", alunoId, cursoId, e);
            throw new RuntimeException("Erro ao calcular progresso: " + e.getMessage(), e);
        }
    }

}