package br.com.oficina.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Configurações que valem para todos os controllers. */
@ControllerAdvice
public class AtributosGlobais {

    /** Nome do usuário logado, exibido no menu. */
    @ModelAttribute("usuarioLogado")
    public String usuarioLogado(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    /** As telas usam isso para esconder botões que só o gerente pode usar (o servidor confere de novo). */
    @ModelAttribute("ehGerente")
    public boolean ehGerente(HttpServletRequest request) {
        return request.isUserInRole("GERENTE");
    }

    /** Tira espaços do começo e do fim de todo texto digitado; texto vazio vira null. */
    @InitBinder
    public void aparaTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
