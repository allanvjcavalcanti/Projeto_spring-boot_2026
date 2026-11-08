package br.com.oficina.seguranca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControleTentativasTest {

    @Test
    void bloqueiaDepoisDeCincoErrosSeguidos() {
        ControleTentativas controle = new ControleTentativas();

        for (int i = 0; i < 4; i++) {
            controle.registrarFalha("fulano");
        }
        assertFalse(controle.estaBloqueado("fulano"));

        controle.registrarFalha("fulano");
        assertTrue(controle.estaBloqueado("fulano"));
    }

    @Test
    void loginComSucessoZeraAContagem() {
        ControleTentativas controle = new ControleTentativas();
        for (int i = 0; i < 4; i++) {
            controle.registrarFalha("fulano");
        }

        controle.limpar("fulano");
        controle.registrarFalha("fulano");

        assertFalse(controle.estaBloqueado("fulano"));
    }

    @Test
    void ignoraMaiusculasEEspacosNoNomeDeUsuario() {
        ControleTentativas controle = new ControleTentativas();
        for (int i = 0; i < 5; i++) {
            controle.registrarFalha("  Fulano ");
        }
        assertTrue(controle.estaBloqueado("fulano"));
    }

    @Test
    void bloqueioDeUmUsuarioNaoAfetaOutro() {
        ControleTentativas controle = new ControleTentativas();
        for (int i = 0; i < 5; i++) {
            controle.registrarFalha("fulano");
        }
        assertFalse(controle.estaBloqueado("beltrano"));
    }
}
