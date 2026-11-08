package br.com.oficina.util;

public final class Documentos {

    private Documentos() {
    }

    /** Remove tudo que não for dígito: "123.456.789-09" vira "12345678909". */
    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /** Confere os dois dígitos verificadores do CPF. */
    public static boolean cpfValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return d.charAt(9) - '0' == digito(d, 9) && d.charAt(10) - '0' == digito(d, 10);
    }

    private static int digito(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
