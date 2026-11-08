package br.com.oficina.controller;

import br.com.oficina.exception.RegraNegocioException;
import org.springframework.validation.BindingResult;

/** Transforma uma RegraNegocioException em erro de formulário (campo ou geral). */
final class ErrosFormulario {

    private ErrosFormulario() {
    }

    static void registrar(BindingResult result, RegraNegocioException e) {
        if (e.getCampo() != null) {
            result.rejectValue(e.getCampo(), "regra", e.getMessage());
        } else {
            result.reject("regra", e.getMessage());
        }
    }
}
