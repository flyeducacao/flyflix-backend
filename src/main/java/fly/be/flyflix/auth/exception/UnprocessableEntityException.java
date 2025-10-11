package fly.be.flyflix.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnprocessableEntityException extends ResponseStatusException {
    public UnprocessableEntityException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}