import fly.be.flyflix.auth.util.CpfValidator;
import fly.be.flyflix.conteudo.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {
    @Test
    void deveAceitarCpfValido() {
        assertDoesNotThrow(() -> CpfValidator.validarCpf("705.678.550-66"));
        assertDoesNotThrow(() -> CpfValidator.validarCpf("70567855066"));
    }

    @Test
    void deveRejeitarCpfComDigitosIguais() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            CpfValidator.validarCpf("111.111.111-11")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("cpf"));
    }

    @Test
    void deveRejeitarCpfComTamanhoInvalido() {
        assertThrows(BadRequestException.class, () -> CpfValidator.validarCpf("1234567890"));
        assertThrows(BadRequestException.class, () -> CpfValidator.validarCpf("123456789012"));
    }

    @Test
    void deveRejeitarCpfComDigitosVerificadoresErrados() {
        assertThrows(BadRequestException.class, () -> CpfValidator.validarCpf("529.982.247-26"));
    }

    @Test
    void deveRejeitarCpfNulo() {
        assertThrows(BadRequestException.class, () -> CpfValidator.validarCpf(null));
    }
}
