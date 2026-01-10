package fly.be.flyflix.auth.controller.dto.admin;

import fly.be.flyflix.auth.entity.Admin;

public record DadosAdminResponse(
        Long id,
        String nome,
        String email,
        String cpf,
        boolean ativo
) {
    public static DadosAdminResponse by(Admin admin) {
        return new DadosAdminResponse(admin.getId(), admin.getNome(), admin.getEmail(), admin.getCpf(), admin.getAtivo());
    }
}
