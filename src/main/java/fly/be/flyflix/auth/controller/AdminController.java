
package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.admin.AtualizarAdminRequest;
import fly.be.flyflix.auth.controller.dto.admin.CadastroAdmin;
import fly.be.flyflix.auth.controller.dto.admin.DadosAdminResponse;
import fly.be.flyflix.auth.entity.Admin;
import fly.be.flyflix.auth.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping
    public ResponseEntity<Void> cadastrar(@RequestBody CadastroAdmin dados) {
        adminService.cadastrarAdmin(dados);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> atualizar(@RequestBody AtualizarAdminRequest dados) {
        adminService.atualizarAdmin(dados);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        adminService.removerAdmin(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosAdminResponse> obter(@PathVariable Long id) {
        DadosAdminResponse response = adminService.obterAdmin(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<Admin>> listar(Pageable paginacao) {
        Page<Admin> response = adminService.listarAdmins(paginacao);

        return ResponseEntity.ok(response);
    }
}
