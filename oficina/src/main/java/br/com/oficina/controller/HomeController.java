package br.com.oficina.controller;

import br.com.oficina.service.PainelService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PainelService painelService;

    public HomeController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping("/")
    public String painel(HttpServletRequest request, Model model) {
        model.addAttribute("totalClientes", painelService.totalClientes());
        model.addAttribute("totalVeiculos", painelService.totalVeiculos());
        model.addAttribute("porStatus", painelService.ordensPorStatus());
        model.addAttribute("ultimas", painelService.ultimasOrdens());
        model.addAttribute("ranking", painelService.servicosMaisPedidos());

        // o valor faturado só é calculado e enviado para a tela quando quem pediu é o gerente
        if (request.isUserInRole("GERENTE")) {
            model.addAttribute("faturamento", painelService.faturamento());
        }
        return "index";
    }
}
