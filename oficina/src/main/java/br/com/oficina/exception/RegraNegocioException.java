package br.com.oficina.exception;

/**
 * Lançada pelos services quando uma regra de negócio é violada
 * (CPF repetido, ordem já concluída, etc.).
 * O campo é opcional: se vier preenchido, o erro aparece embaixo daquele input.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String campo;

    public RegraNegocioException(String mensagem) {
        this(null, mensagem);
    }

    public RegraNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
