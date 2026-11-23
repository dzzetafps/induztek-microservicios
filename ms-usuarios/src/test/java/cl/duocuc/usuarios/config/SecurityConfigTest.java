
package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.RolDto;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private UsuarioRepository repositorio;
    private RolClient roles;
    private UserDetailsService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepository.class);
        roles = mock(RolClient.class);

        SecurityConfig config = new SecurityConfig();
        servicio = config.userDetailsService(repositorio, roles);
    }

    private Usuario crearUsuario() {
        return Usuario.builder()
                .id(1L)
                .nombre("Jefe Tecnico")
                .correo("jefe@induztek.local")
                .contrasena("hash-guardado")
                .rolId(3L)
                .build();
    }

    @Test
    void cargarUsuarioCorrectamente() {
        when(repositorio.findByCorreo("jefe@induztek.local"))
                .thenReturn(Optional.of(crearUsuario()));

        when(roles.obtener(3L))
                .thenReturn(new RolDto(3L, "JEFE_TECNICO"));

        UserDetails usuario =
                servicio.loadUserByUsername("jefe@induztek.local");

        assertEquals("jefe@induztek.local", usuario.getUsername());
        assertEquals("hash-guardado", usuario.getPassword());

        assertTrue(usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_JEFE_TECNICO")));
    }

    @Test
    void rechazarUsuarioInexistente() {
        when(repositorio.findByCorreo("noexiste@induztek.local"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> servicio.loadUserByUsername(
                        "noexiste@induztek.local")
        );
    }

    @Test
    void rechazarRolInexistente() {
        when(repositorio.findByCorreo("jefe@induztek.local"))
                .thenReturn(Optional.of(crearUsuario()));

        when(roles.obtener(3L)).thenReturn(null);

        assertThrows(
                UsernameNotFoundException.class,
                () -> servicio.loadUserByUsername(
                        "jefe@induztek.local")
        );
    }

    @Test
    void cargarRolTecnico() {
        Usuario tecnico = Usuario.builder()
                .id(2L)
                .nombre("Tecnico")
                .correo("tecnico@induztek.local")
                .contrasena("hash-tecnico")
                .rolId(1L)
                .build();

        when(repositorio.findByCorreo("tecnico@induztek.local"))
                .thenReturn(Optional.of(tecnico));

        when(roles.obtener(1L))
                .thenReturn(new RolDto(1L, "TECNICO"));

        UserDetails usuario =
                servicio.loadUserByUsername("tecnico@induztek.local");

        assertTrue(usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_TECNICO")));

        assertFalse(usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_JEFE_TECNICO")));
    }
}
