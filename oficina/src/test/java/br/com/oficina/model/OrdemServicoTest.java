package br.com.oficina.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrdemServicoTest {

    @Test
    void totalSomaPrecoVezesQuantidadeDeTodosOsItens() {
        OrdemServico ordem = new OrdemServico();
        ordem.adicionarItem(new ItemOrdem(new Servico(), 2, new BigDecimal("129.90")));
        ordem.adicionarItem(new ItemOrdem(new Servico(), 1, new BigDecimal("120.00")));

        // 2 x 129,90 + 1 x 120,00 = 379,80
        assertEquals(0, new BigDecimal("379.80").compareTo(ordem.getTotal()));
    }

    @Test
    void ordemSemItensTemTotalZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(new OrdemServico().getTotal()));
    }
}
