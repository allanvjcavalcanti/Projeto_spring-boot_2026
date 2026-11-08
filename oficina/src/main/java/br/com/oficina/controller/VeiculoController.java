package br.com.oficina.controller;

import br.com.oficina.dto.VeiculoForm;
import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.Cliente;
import br.com.oficina.model.Veiculo;
import br.com.oficina.service.ClienteService;
import br.com.oficina.service.OrdemServicoService;
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

import java.util.List;

@Controller
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final ClienteService clienteService;
    private final OrdemServicoService ordemService;

    public VeiculoController(VeiculoService veiculoService,
                             ClienteService clienteService,
                             OrdemServicoService ordemService) {
        this.veiculoService = veiculoService;
        this.clienteService = clienteService;
        this.ordemService = ordemService;
    }

    @InitBinder("form")
    public void camposPermitidos(WebDataBinder binder) {
        binder.setAllowedFields("id", "placa", "marca", "modelo", "ano", "cor", "clienteId");
    }

    /** Roda antes de qualquer método abaixo: o select de donos aparece em todo formulário. */
    @ModelAttribute("clientes")
    public List<Cliente> clientes() {
        return clienteService.listar(null);
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("veiculos", veiculoService.listar());
        return "veiculos/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long clienteId, Model model) {
        VeiculoForm form = new VeiculoForm();
        form.setClienteId(clienteId);
        model.addAttribute("form", form);
        return "veiculos/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Veiculo veiculo = veiculoService.buscar(id);
        model.addAttribute("veiculo", veiculo);
        model.addAttribute("ordens", ordemService.listarDoVeiculo(id));
        return "veiculos/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", veiculoService.paraForm(id));
        return "veiculos/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("form") VeiculoForm form,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Veiculo salvo = veiculoService.salvar(form);
                redirect.addFlashAttribute("sucesso", "Veículo " + salvo.getPlaca() + " salvo com sucesso");
                return "redirect:/veiculos/" + salvo.getId();
            } catch (RegraNegocioException e) {
                ErrosFormulario.registrar(result, e);
            }
        }
        return "veiculos/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            veiculoService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Veículo excluído");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/veiculos";
    }
}
