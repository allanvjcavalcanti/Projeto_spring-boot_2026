package br.com.oficina;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OficinaApplicationTests {

    // sobe o Spring inteiro (banco, JPA, segurança, controllers, dados iniciais); falha se algo estiver mal configurado
    @Test
    void contextLoads() {
    }
}
