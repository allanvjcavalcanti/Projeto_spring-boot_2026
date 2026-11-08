package br.com.oficina.controller;

import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.Cliente;
import br.com.oficina.service.ClienteService;
import br.com.oficina.service.VeiculoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final VeiculoService veiculoService;

    public ClienteController(ClienteService clienteService, VeiculoService veiculoService) {
        this.clienteService = clienteService;
        this.veiculoService = veiculoService;
    }

    /** Só estes campos do formulário são aceitos; qualquer outro enviado à mão é ignorado. */
    @InitBinder("cliente")
    public void camposPermitidos(WebDataBinder binder) {
        binder.setAllowedFields("id", "nome", "cpf", "telefone", "email");
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("clientes", clienteService.listar(busca));
        model.addAttribute("busca", busca);
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscar(id));
        model.addAttribute("veiculos", veiculoService.listarDoCliente(id));
        return "clientes/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscar(id));
        return "clientes/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("cliente") Cliente cliente,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Cliente salvo = clienteService.salvar(cliente);
                redirect.addFlashAttribute("sucesso", "Cliente " + salvo.getNome() + " salvo com sucesso");
                return "redirect:/clientes/" + salvo.getId();
            } catch (RegraNegocioException e) {
                ErrosFormulario.registrar(result, e);
            }
        }
        return "clientes/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            clienteService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Cliente excluído");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes";
    }
}
