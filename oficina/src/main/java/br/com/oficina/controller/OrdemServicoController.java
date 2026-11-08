package br.com.oficina.controller;

import br.com.oficina.dto.ItemForm;
import br.com.oficina.dto.OrdemForm;
import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.OrdemServico;
import br.com.oficina.model.StatusOrdem;
import br.com.oficina.service.OrdemServicoService;
import br.com.oficina.service.ServicoService;
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
@RequestMapping("/ordens")
public class OrdemServicoController {

    private final OrdemServicoService ordemService;
    private final VeiculoService veiculoService;
    private final ServicoService servicoService;

    public OrdemServicoController(OrdemServicoService ordemService,
                                  VeiculoService veiculoService,
                                  ServicoService servicoService) {
        this.ordemService = ordemService;
        this.veiculoService = veiculoService;
        this.servicoService = servicoService;
    }

    @InitBinder("form")
    public void camposDaOrdem(WebDataBinder binder) {
        binder.setAllowedFields("veiculoId", "descricaoProblema");
    }

    @InitBinder("itemForm")
    public void camposDoItem(WebDataBinder binder) {
        binder.setAllowedFields("servicoId", "quantidade");
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusOrdem status, Model model) {
        model.addAttribute("ordens", ordemService.listar(status));
        model.addAttribute("filtro", status);
        model.addAttribute("todosStatus", StatusOrdem.values());
        return "ordens/lista";
    }

    @GetMapping("/nova")
    public String nova(@RequestParam(required = false) Long veiculoId, Model model) {
        OrdemForm form = new OrdemForm();
        form.setVeiculoId(veiculoId);
        model.addAttribute("form", form);
        model.addAttribute("veiculos", veiculoService.listar());
        return "ordens/nova";
    }

    @PostMapping
    public String abrir(@Valid @ModelAttribute("form") OrdemForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                OrdemServico ordem = ordemService.abrir(form);
                redirect.addFlashAttribute("sucesso", "Ordem #" + ordem.getId() + " aberta");
                return "redirect:/ordens/" + ordem.getId();
            } catch (RegraNegocioException e) {
                ErrosFormulario.registrar(result, e);
            }
        }
        model.addAttribute("veiculos", veiculoService.listar());
        return "ordens/nova";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("itemForm", new ItemForm());
        carregarDetalhe(id, model);
        return "ordens/detalhe";
    }

    @PostMapping("/{id}/itens")
    public String adicionarItem(@PathVariable Long id,
                                @Valid @ModelAttribute("itemForm") ItemForm itemForm,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                ordemService.adicionarItem(id, itemForm);
                redirect.addFlashAttribute("sucesso", "Serviço adicionado à ordem");
                return "redirect:/ordens/" + id;
            } catch (RegraNegocioException e) {
                ErrosFormulario.registrar(result, e);
            }
        }
        carregarDetalhe(id, model);
        return "ordens/detalhe";
    }

    @PostMapping("/{id}/itens/{itemId}/remover")
    public String removerItem(@PathVariable Long id,
                              @PathVariable Long itemId,
                              RedirectAttributes redirect) {
        try {
            ordemService.removerItem(id, itemId);
            redirect.addFlashAttribute("sucesso", "Item removido");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/ordens/" + id;
    }

    @PostMapping("/{id}/status")
    public String mudarStatus(@PathVariable Long id,
                              @RequestParam StatusOrdem novo,
                              RedirectAttributes redirect) {
        // cancelar tem rota própria (/cancelar), liberada só para o gerente na SegurancaConfig
        if (novo == StatusOrdem.CANCELADA) {
            redirect.addFlashAttribute("erro", "Somente o gerente pode cancelar uma ordem");
            return "redirect:/ordens/" + id;
        }
        try {
            ordemService.mudarStatus(id, novo);
            redirect.addFlashAttribute("sucesso", "Status alterado para " + novo.getRotulo());
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/ordens/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            ordemService.mudarStatus(id, StatusOrdem.CANCELADA);
            redirect.addFlashAttribute("sucesso", "Ordem cancelada");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/ordens/" + id;
    }

    private void carregarDetalhe(Long id, Model model) {
        model.addAttribute("ordem", ordemService.buscar(id));
        model.addAttribute("servicos", servicoService.listarAtivos());
    }
}
