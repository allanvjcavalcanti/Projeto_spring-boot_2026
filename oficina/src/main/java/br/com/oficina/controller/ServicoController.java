package br.com.oficina.controller;

import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.Servico;
import br.com.oficina.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @InitBinder("servico")
    public void camposPermitidos(WebDataBinder binder) {
        binder.setAllowedFields("id", "nome", "descricao", "preco", "ativo");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicos", servicoService.listar());
        return "servicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("servico", new Servico());
        return "servicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("servico", servicoService.buscar(id));
        return "servicos/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("servico") Servico servico,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Servico salvo = servicoService.salvar(servico);
                redirect.addFlashAttribute("sucesso", "Serviço \"" + salvo.getNome() + "\" salvo com sucesso");
                return "redirect:/servicos";
            } catch (RegraNegocioException e) {
                ErrosFormulario.registrar(result, e);
            }
        }
        return "servicos/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            servicoService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Serviço excluído");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/servicos";
    }
}
