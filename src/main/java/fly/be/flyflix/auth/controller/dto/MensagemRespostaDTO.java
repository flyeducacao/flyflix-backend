package fly.be.flyflix.auth.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MensagemRespostaDTO {

    private String mensagem;
    private boolean sucesso;
    private int status;
    private String codigoErro;
    private LocalDateTime timestamp;

    public MensagemRespostaDTO(String mensagem, boolean sucesso, int status, String codigoErro) {
        this.mensagem = mensagem;
        this.sucesso = sucesso;
        this.status = status;
        this.codigoErro = codigoErro;
        this.timestamp = LocalDateTime.now();
    }


}


