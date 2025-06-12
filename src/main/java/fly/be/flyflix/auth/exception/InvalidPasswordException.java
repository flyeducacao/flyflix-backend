package fly.be.flyflix.auth.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Senha inválida.");
    }
}
