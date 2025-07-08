
package fly.be.flyflix.auth.controller;

import fly.be.flyflix.auth.controller.dto.admin.AtualizarAdminRequest;
import fly.be.flyflix.auth.controller.dto.admin.CadastroAdmin;
import fly.be.flyflix.auth.entity.Admin;
import fly.be.flyflix.auth.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> cadastrar(@RequestBody CadastroAdmin dados) {
        return adminService.cadastrarAdmin(dados);
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> atualizar(@RequestBody AtualizarAdminRequest dados) {
        return adminService.atualizarAdmin(dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> remover(@PathVariable Long id) {
        return adminService.removerAdmin(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obter(@PathVariable Long id) {
        return adminService.obterAdmin(id);
    }

    @GetMapping
    public Page<Admin> listar(Pageable paginacao) {
        return adminService.listarAdmins(paginacao);
    }
}
