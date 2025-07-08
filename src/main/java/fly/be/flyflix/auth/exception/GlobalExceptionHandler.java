package fly.be.flyflix.auth.exception;

import fly.be.flyflix.auth.controller.dto.MensagemRespostaDTO;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import fly.be.flyflix.conteudo.exceptions.DefaultMessageError;
import fly.be.flyflix.conteudo.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
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

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<DefaultMessageError> handlerBadRequestException(BadRequestException e) {
        DefaultMessageError error = new DefaultMessageError(e.getStatusCode().value(), e.getReason());

        return ResponseEntity.status(e.getStatusCode()).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DefaultMessageError> handlerNotFoundException(NotFoundException e) {
        DefaultMessageError error = new DefaultMessageError(e.getStatusCode().value(), e.getReason());

        return ResponseEntity.status(e.getStatusCode()).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DefaultMessageError> handlerAuthenticationException(AuthenticationException e) {
        DefaultMessageError error = new DefaultMessageError(HttpStatus.UNAUTHORIZED.value(), "Email ou senha incorretos");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
