import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SenhaValidatorTest {

    private boolean isSenhaValida(String senha) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=<>?{}\\[\\]~]).{8,}$";
        return senha != null && senha.matches(regex);
    }

    @Test
    void senhaValida_deveRetornarTrue() {
        assertTrue(isSenhaValida("Senha123!"));
        assertTrue(isSenhaValida("A1@aaaaaa"));
    }

    @Test
    void senhaInvalida_muitoCurta() {
        assertFalse(isSenhaValida("A1@a"));
    }

    @Test
    void senhaInvalida_semLetraMaiuscula() {
        assertFalse(isSenhaValida("senha123!"));
    }

    @Test
    void senhaInvalida_semNumero() {
        assertFalse(isSenhaValida("Senha!@#"));
    }

    @Test
    void senhaInvalida_semEspecial() {
        assertFalse(isSenhaValida("Senha123"));
    }

    @Test
    void senhaInvalida_nula() {
        assertFalse(isSenhaValida(null));
    }
}

