package br.com.oficina.seguranca;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção contra tentativa de adivinhar senha: depois de 5 erros seguidos, o usuário
 * fica bloqueado por 10 minutos (mesmo que acerte a senha nesse período).
 * Fica na memória: reiniciar a aplicação zera o controle.
 */
@Component
public class ControleTentativas {

    static final int MAXIMO_DE_FALHAS = 5;
    static final Duration TEMPO_DE_BLOQUEIO = Duration.ofMinutes(10);
    private static final int LIMITE_DE_REGISTROS = 5000;

    private static final class Registro {
        private final int falhas;
        private final Instant ultimaFalha;

        private Registro(int falhas, Instant ultimaFalha) {
            this.falhas = falhas;
            this.ultimaFalha = ultimaFalha;
        }
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public void registrarFalha(String usuario) {
        if (registros.size() > LIMITE_DE_REGISTROS) {
            // evita crescer sem limite se alguém tentar milhares de nomes diferentes
            registros.clear();
        }
        registros.merge(chave(usuario), new Registro(1, Instant.now()),
                (antigo, novo) -> new Registro(antigo.falhas + 1, Instant.now()));
    }

    public void limpar(String usuario) {
        registros.remove(chave(usuario));
    }

    public boolean estaBloqueado(String usuario) {
        String chave = chave(usuario);
        Registro registro = registros.get(chave);
        if (registro == null || registro.falhas < MAXIMO_DE_FALHAS) {
            return false;
        }
        if (registro.ultimaFalha.plus(TEMPO_DE_BLOQUEIO).isBefore(Instant.now())) {
            registros.remove(chave);
            return false;
        }
        return true;
    }

    @EventListener
    public void aoErrarASenha(AuthenticationFailureBadCredentialsEvent evento) {
        registrarFalha(evento.getAuthentication().getName());
    }

    @EventListener
    public void aoEntrar(AuthenticationSuccessEvent evento) {
        limpar(evento.getAuthentication().getName());
    }

    private String chave(String usuario) {
        return usuario == null ? "" : usuario.trim().toLowerCase(Locale.ROOT);
    }
}
