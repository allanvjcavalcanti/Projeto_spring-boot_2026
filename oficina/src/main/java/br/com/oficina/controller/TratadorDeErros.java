package br.com.oficina.controller;

import br.com.oficina.exception.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Transforma erros "técnicos" em uma página amigável, sem mostrar detalhes internos
 * (stack trace, nome de tabela, SQL) para quem está usando o sistema.
 */
@ControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String regraDeNegocio(RegraNegocioException e, Model model) {
        return pagina(model, "Não foi possível concluir", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String parametroInvalido(Model model) {
        return pagina(model, "Pedido inválido", "Um dos valores enviados não é válido. Volte e tente novamente.");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String alteradoPorOutraPessoa(Model model) {
        return pagina(model, "Registro alterado por outra pessoa",
                "Alguém alterou este registro ao mesmo tempo. Recarregue a página e refaça a operação.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflitoNoBanco(DataIntegrityViolationException e, Model model) {
        log.warn("Violação de integridade no banco: {}", e.getMostSpecificCause().getMessage());
        return pagina(model, "Operação não permitida",
                "Já existe um registro igual a este, ou ele está sendo usado por outro cadastro.");
    }

    private String pagina(Model model, String titulo, String mensagem) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("mensagem", mensagem);
        return "erro-regra";
    }
}
