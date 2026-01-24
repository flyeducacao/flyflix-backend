package fly.be.flyflix.auth.service;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SenhaGenerator {

    private static final String MAIUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMEROS = "0123456789";
    private static final String ESPECIAIS = "!@#$%&*";
    private static final String TODOS = MAIUSCULAS + MINUSCULAS + NUMEROS + ESPECIAIS;

    private static final int TAMANHO_MINIMO = 8;

    private static final SecureRandom random = new SecureRandom();

    public static String gerarSenhaTemporaria() {
        StringBuilder senha = new StringBuilder();

        // Garantir pelo menos 1 de cada tipo
        senha.append(pegarAleatorio(MAIUSCULAS));
        senha.append(pegarAleatorio(MINUSCULAS));
        senha.append(pegarAleatorio(NUMEROS));
        senha.append(pegarAleatorio(ESPECIAIS));

        // Preencher até o tamanho mínimo
        for (int i = senha.length(); i < TAMANHO_MINIMO; i++) {
            senha.append(pegarAleatorio(TODOS));
        }

        // Embaralhar os caracteres
        List<Character> caracteres = senha.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(caracteres);

        return caracteres.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private static char pegarAleatorio(String fonte) {
        return fonte.charAt(random.nextInt(fonte.length()));
    }
}
