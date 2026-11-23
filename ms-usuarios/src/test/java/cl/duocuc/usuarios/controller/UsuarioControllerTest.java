
package cl.duocuc.usuarios.controller;

import cl.duocuc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duocuc.usuarios.dto.CambiarContrasenaRequest;
import cl.duocuc.usuarios.dto.RegistroRequest;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.service.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {

    private UsuarioService servicio;
    private UsuarioController controlador;

    private Authentication jefe;
    private Authentication tecnico;

    @BeforeEach
    void preparar() {
        servicio = mock(UsuarioService.class);
        controlador = new UsuarioController(servicio);

        jefe = new UsernamePasswordAuthenticationToken(
                "jefe@induztek.local",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_JEFE_TECNICO"))
        );

        tecnico = new UsernamePasswordAuthenticationToken(
                "tecnico@induztek.local",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TECNICO"))
        );
    }

    private UsuarioResponse crearUsuario(
            Long id, String correo, Long rolId) {

        return new UsuarioResponse(
                id,
                "Usuario Prueba",
                correo,
                "Santiago",
                rolId,
                rolId == 3L ? "JEFE_TECNICO" : "TECNICO",
                null
        );
    }

    @Test
    void jefePuedeListarUsuarios() {
        UsuarioResponse usuario =
                crearUsuario(1L, "jefe@induztek.local", 3L);

        when(servicio.listar(null))
                .thenReturn(List.of(usuario));

        List<UsuarioResponse> resultado =
                controlador.listar(null, jefe);

        assertEquals(1, resultado.size());
        verify(servicio).listar(null);
    }

    @Test
    void tecnicoNoPuedeListarUsuarios() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controlador.listar(null, tecnico)
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(servicio, never()).listar(any());
    }

    @Test
    void usuarioPuedeConsultarSuPerfil() {
        UsuarioResponse usuario =
                crearUsuario(2L, "tecnico@induztek.local", 1L);

        when(servicio.obtenerPorCorreo("tecnico@induztek.local"))
                .thenReturn(usuario);

        UsuarioResponse resultado = controlador.miPerfil(tecnico);

        assertEquals("tecnico@induztek.local", resultado.correo());
    }

    @Test
    void tecnicoPuedeConsultarSuUsuario() {
        UsuarioResponse usuario =
                crearUsuario(2L, "tecnico@induztek.local", 1L);

        when(servicio.obtener(2L)).thenReturn(usuario);

        UsuarioResponse resultado = controlador.obtener(2L, tecnico);

        assertEquals(2L, resultado.id());
    }

    @Test
    void tecnicoNoPuedeConsultarOtroUsuario() {
        when(servicio.obtener(3L)).thenReturn(
                crearUsuario(3L, "otro@induztek.local", 1L)
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controlador.obtener(3L, tecnico)
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void jefePuedeRegistrarTecnico() {
        RegistroRequest datos = new RegistroRequest(
                "Tecnico Nuevo",
                "nuevo@induztek.local",
                "clave123",
                "Santiago",
                1L
        );

        UsuarioResponse usuario =
                crearUsuario(4L, "nuevo@induztek.local", 1L);

        when(servicio.registrar(datos)).thenReturn(usuario);

        UsuarioResponse resultado =
                controlador.registrar(datos, jefe);

        assertEquals(4L, resultado.id());
        verify(servicio).registrar(datos);
    }

    @Test
    void tecnicoNoPuedeRegistrarUsuarios() {
        RegistroRequest datos = new RegistroRequest(
                "Usuario",
                "otro@induztek.local",
                "clave123",
                "Santiago",
                1L
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controlador.registrar(datos, tecnico)
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(servicio, never()).registrar(any());
    }

    @Test
    void tecnicoPuedeActualizarSuPerfil() {
        UsuarioResponse usuario =
                crearUsuario(2L, "tecnico@induztek.local", 1L);

        ActualizarUsuarioRequest datos =
                new ActualizarUsuarioRequest(
                        "Nombre Nuevo",
                        "tecnico@induztek.local",
                        "Valparaiso",
                        null
                );

        when(servicio.obtener(2L)).thenReturn(usuario);
        when(servicio.actualizar(2L, datos)).thenReturn(usuario);

        controlador.actualizar(2L, datos, tecnico);

        verify(servicio).actualizar(2L, datos);
    }

    @Test
    void noSePuedeModificarElRol() {
        UsuarioResponse usuario =
                crearUsuario(2L, "tecnico@induztek.local", 1L);

        ActualizarUsuarioRequest datos =
                new ActualizarUsuarioRequest(
                        "Usuario",
                        "tecnico@induztek.local",
                        "Santiago",
                        3L
                );

        when(servicio.obtener(2L)).thenReturn(usuario);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controlador.actualizar(2L, datos, tecnico)
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(servicio, never()).actualizar(anyLong(), any());
    }

    @Test
    void tecnicoPuedeCambiarSuContrasena() {
        when(servicio.obtener(2L)).thenReturn(
                crearUsuario(2L, "tecnico@induztek.local", 1L)
        );

        CambiarContrasenaRequest datos =
                mock(CambiarContrasenaRequest.class);

        controlador.cambiarContrasena(2L, datos, tecnico);

        verify(servicio).cambiarContrasena(2L, datos);
    }

    @Test
    void tecnicoNoPuedeCambiarContrasenaAjena() {
        when(servicio.obtener(3L)).thenReturn(
                crearUsuario(3L, "otro@induztek.local", 1L)
        );

        CambiarContrasenaRequest datos =
                mock(CambiarContrasenaRequest.class);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controlador.cambiarContrasena(3L, datos, tecnico)
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(servicio, never()).cambiarContrasena(anyLong(), any());
    }

    @Test
    void jefePuedeEliminarUsuario() {
        controlador.eliminar(2L, jefe);

        verify(servicio).eliminar(2L);
    }

    @Test
    void tecnicoNoPuedeEliminarUsuarios() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controlador.eliminar(2L, tecnico)
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(servicio, never()).eliminar(anyLong());
    }
}
