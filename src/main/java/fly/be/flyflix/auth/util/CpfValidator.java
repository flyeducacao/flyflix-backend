package fly.be.flyflix.auth.util;

import fly.be.flyflix.conteudo.exceptions.BadRequestException;

public class CpfValidator {
    public static void validarCpf(String cpf) {
        if (cpf == null) throw new BadRequestException("CPF não pode ser nulo");
        String cpfNum = cpf.replaceAll("\\D", "");
        if (cpfNum.length() != 11 || cpfNum.chars().distinct().count() == 1) {
            throw new BadRequestException("CPF inválido");
        }
        try {
            int d1 = 0, d2 = 0;
            for (int i = 0; i < 9; i++) {
                int digito = Character.getNumericValue(cpfNum.charAt(i));
                d1 += digito * (10 - i);
                d2 += digito * (11 - i);
            }
            int resto1 = d1 % 11;
            int verificador1 = (resto1 < 2) ? 0 : 11 - resto1;
            d2 += verificador1 * 2;
            int resto2 = d2 % 11;
            int verificador2 = (resto2 < 2) ? 0 : 11 - resto2;
            if (verificador1 != Character.getNumericValue(cpfNum.charAt(9)) ||
                verificador2 != Character.getNumericValue(cpfNum.charAt(10))) {
                throw new BadRequestException("CPF inválido");
            }
        } catch (Exception e) {
            throw new BadRequestException("CPF inválido");
        }
    }
}
