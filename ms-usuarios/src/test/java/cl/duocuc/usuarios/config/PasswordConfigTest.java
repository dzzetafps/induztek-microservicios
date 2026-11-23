
package cl.duocuc.usuarios.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    @Test
    void contrasenaCorrecta() {
        PasswordConfig config = new PasswordConfig();
        PasswordEncoder encoder = config.passwordEncoder();

        String contrasena = "clave123";
        String hash = encoder.encode(contrasena);

        assertNotEquals(contrasena, hash);
        assertTrue(encoder.matches(contrasena, hash));
    }

    @Test
    void contrasenaIncorrecta() {
        PasswordConfig config = new PasswordConfig();
        PasswordEncoder encoder = config.passwordEncoder();

        String hash = encoder.encode("clave123");

        assertFalse(encoder.matches("incorrecta", hash));
    }
}
