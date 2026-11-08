package br.com.oficina.model;

import org.junit.jupiter.api.Test;

import static br.com.oficina.model.StatusOrdem.ABERTA;
import static br.com.oficina.model.StatusOrdem.CANCELADA;
import static br.com.oficina.model.StatusOrdem.CONCLUIDA;
import static br.com.oficina.model.StatusOrdem.EM_ANDAMENTO;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusOrdemTest {

    @Test
    void ordemAbertaPodeIniciarOuCancelar() {
        assertTrue(ABERTA.podeIrPara(EM_ANDAMENTO));
        assertTrue(ABERTA.podeIrPara(CANCELADA));
    }

    @Test
    void naoPulaEtapa() {
        assertFalse(ABERTA.podeIrPara(CONCLUIDA));
    }

    @Test
    void ordemEncerradaNaoMudaMais() {
        assertTrue(CONCLUIDA.proximos().isEmpty());
        assertTrue(CANCELADA.proximos().isEmpty());
    }

    @Test
    void soPermiteEditarItensEnquantoNaoEncerrada() {
        assertTrue(ABERTA.isEditavel());
        assertTrue(EM_ANDAMENTO.isEditavel());
        assertFalse(CONCLUIDA.isEditavel());
        assertFalse(CANCELADA.isEditavel());
    }
}
