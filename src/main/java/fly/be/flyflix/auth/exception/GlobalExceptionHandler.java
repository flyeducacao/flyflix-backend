package fly.be.flyflix.auth.exception;

import fly.be.flyflix.auth.controller.dto.MensagemRespostaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<MensagemRespostaDTO> handleEmailNotFound(EmailNotFoundException ex) {
        MensagemRespostaDTO dto = new MensagemRespostaDTO(
                ex.getMessage(),
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "EMAIL_NAO_ENCONTRADO"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<MensagemRespostaDTO> handleInvalidPassword(InvalidPasswordException ex) {
        MensagemRespostaDTO dto = new MensagemRespostaDTO(
                ex.getMessage(),
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "SENHA_INVALIDA"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensagemRespostaDTO> handleGenericException(Exception ex) {
        MensagemRespostaDTO dto = new MensagemRespostaDTO(
                "Erro interno. Contate o suporte.",
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERRO_INTERNO"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }
}
