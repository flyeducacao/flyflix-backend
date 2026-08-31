package fly.be.flyflix.auth.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException() {
        super("E-mail inválido.");
    }
}


