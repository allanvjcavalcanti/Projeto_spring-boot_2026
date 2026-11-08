package br.com.oficina.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Formulário para adicionar um serviço a uma ordem. */
public class ItemForm {

    @NotNull(message = "Escolha um serviço")
    private Long servicoId;

    @NotNull(message = "Informe a quantidade")
    @Min(value = 1, message = "A quantidade mínima é 1")
    @Max(value = 99, message = "A quantidade máxima é 99")
    private Integer quantidade = 1;

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
