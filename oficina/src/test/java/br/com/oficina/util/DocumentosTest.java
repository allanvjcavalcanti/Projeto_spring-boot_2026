package br.com.oficina.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentosTest {

    @Test
    void aceitaCpfValidoComOuSemMascara() {
        assertTrue(Documentos.cpfValido("529.982.247-25"));
        assertTrue(Documentos.cpfValido("52998224725"));
    }

    @Test
    void aceitaOsCpfsDosDadosIniciais() {
        assertTrue(Documentos.cpfValido("111.444.777-35"));
        assertTrue(Documentos.cpfValido("390.533.447-05"));
    }

    @Test
    void recusaDigitoVerificadorErrado() {
        assertFalse(Documentos.cpfValido("529.982.247-24"));
    }

    @Test
    void recusaSequenciaDeDigitosIguais() {
        assertFalse(Documentos.cpfValido("111.111.111-11"));
    }

    @Test
    void recusaTamanhoErradoOuNulo() {
        assertFalse(Documentos.cpfValido("123"));
        assertFalse(Documentos.cpfValido(null));
    }

    @Test
    void somenteDigitosRemoveAMascara() {
        assertEquals("12345678909", Documentos.somenteDigitos("123.456.789-09"));
    }
}
