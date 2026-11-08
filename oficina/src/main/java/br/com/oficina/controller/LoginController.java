package br.com.oficina.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class LoginController {

    /** O POST do login é tratado pelo Spring Security, aqui só mostramos a página. */
    @GetMapping("/login")
    public String login(Principal principal) {
        return principal != null ? "redirect:/" : "login";
    }
}
