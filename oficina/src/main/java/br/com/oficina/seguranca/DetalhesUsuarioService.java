package br.com.oficina.seguranca;

import br.com.oficina.model.Usuario;
import br.com.oficina.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Ensina o Spring Security a buscar o usuário na tabela "usuarios". */
@Service
public class DetalhesUsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ControleTentativas controleTentativas;

    public DetalhesUsuarioService(UsuarioRepository usuarioRepository, ControleTentativas controleTentativas) {
        this.usuarioRepository = usuarioRepository;
        this.controleTentativas = controleTentativas;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String login = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByUsername(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenhaHash())
                .roles(usuario.getPerfil().name())
                .disabled(!usuario.isAtivo())
                .accountLocked(controleTentativas.estaBloqueado(login))
                .build();
    }
}
