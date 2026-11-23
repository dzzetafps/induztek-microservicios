
package cl.duocuc.usuarios.service;

import cl.duocuc.usuarios.dto.RegistroRequest;
import cl.duocuc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.CambiarContrasenaRequest;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.RolDto;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository repositorio;
    private RolClient rolClient;
    private AlmacenamientoService almacenamiento;
    private PasswordEncoder passwordEncoder;
    private UsuarioService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepository.class);
        rolClient = mock(RolClient.class);
        almacenamiento = mock(AlmacenamientoService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        servicio = new UsuarioService(
                repositorio,
                rolClient,
                almacenamiento,
                passwordEncoder
        );
    }

    private Usuario crearUsuario() {
        return Usuario.builder()
                .id(1L)
                .nombre("Jefe Tecnico")
                .correo("jefe@induztek.local")
                .ciudad("Santiago")
                .contrasena("hash-guardado")
                .rolId(3L)
                .build();
    }

    private void prepararRol() {
        when(rolClient.obtener(3L))
                .thenReturn(new RolDto(3L, "JEFE_TECNICO"));
    }

    @Test
    void obtenerUsuarioExistente() {
        when(repositorio.findById(1L))
                .thenReturn(Optional.of(crearUsuario()));
        prepararRol();

        UsuarioResponse resultado = servicio.obtener(1L);

        assertEquals("Jefe Tecnico", resultado.nombre());
        assertEquals("JEFE_TECNICO", resultado.rolNombre());
    }

    @Test
    void rechazarUsuarioInexistente() {
        when(repositorio.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.obtener(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void obtenerUsuarioPorCorreo() {
        when(repositorio.findByCorreo("jefe@induztek.local"))
                .thenReturn(Optional.of(crearUsuario()));
        prepararRol();

        UsuarioResponse resultado =
                servicio.obtenerPorCorreo("jefe@induztek.local");

        assertEquals(1L, resultado.id());
        assertEquals("jefe@induztek.local", resultado.correo());
    }

    @Test
    void iniciarSesionCorrectamente() {
        LoginRequest datos = mock(LoginRequest.class);

        when(datos.correo()).thenReturn("jefe@induztek.local");
        when(datos.contrasena()).thenReturn("clave123");

        when(repositorio.findByCorreo("jefe@induztek.local"))
                .thenReturn(Optional.of(crearUsuario()));

        when(passwordEncoder.matches("clave123", "hash-guardado"))
                .thenReturn(true);

        prepararRol();

        UsuarioResponse resultado = servicio.iniciarSesion(datos);

        assertEquals("Jefe Tecnico", resultado.nombre());
        assertEquals(3L, resultado.rolId());
    }

    @Test
    void rechazarContrasenaIncorrecta() {
        LoginRequest datos = mock(LoginRequest.class);

        when(datos.correo()).thenReturn("jefe@induztek.local");
        when(datos.contrasena()).thenReturn("incorrecta");

        when(repositorio.findByCorreo("jefe@induztek.local"))
                .thenReturn(Optional.of(crearUsuario()));

        when(passwordEncoder.matches("incorrecta", "hash-guardado"))
                .thenReturn(false);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.iniciarSesion(datos)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void rechazarLoginDeUsuarioInexistente() {
        LoginRequest datos = mock(LoginRequest.class);

        when(datos.correo()).thenReturn("noexiste@induztek.local");

        when(repositorio.findByCorreo("noexiste@induztek.local"))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.iniciarSesion(datos)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void cambiarContrasenaCorrectamente() {
        Usuario usuario = crearUsuario();
        CambiarContrasenaRequest datos =
                mock(CambiarContrasenaRequest.class);

        when(datos.actual()).thenReturn("anterior123");
        when(datos.nueva()).thenReturn("nueva123");

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("anterior123", "hash-guardado"))
                .thenReturn(true);

        when(passwordEncoder.encode("nueva123"))
                .thenReturn("nuevo-hash");

        servicio.cambiarContrasena(1L, datos);

        assertEquals("nuevo-hash", usuario.getContrasena());
        verify(repositorio).save(usuario);
    }

    @Test
    void rechazarCambioContrasenaIncorrecta() {
        CambiarContrasenaRequest datos =
                mock(CambiarContrasenaRequest.class);

        when(datos.actual()).thenReturn("incorrecta");

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(crearUsuario()));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cambiarContrasena(1L, datos)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarAvatarInexistente() {
        when(repositorio.findById(1L))
                .thenReturn(Optional.of(crearUsuario()));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.obtenerAvatar(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void registrarTecnicoCorrectamente() {
        RegistroRequest datos = new RegistroRequest(
                "Tecnico Prueba",
                "tecnico@induztek.local",
                "clave123",
                "Santiago",
                1L
        );

        when(repositorio.existsByCorreo(datos.correo()))
                .thenReturn(false);

        when(rolClient.obtener(1L))
                .thenReturn(new RolDto(1L, "TECNICO"));

        when(passwordEncoder.encode("clave123"))
                .thenReturn("hash-seguro");

        when(repositorio.save(any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario usuario = invocacion.getArgument(0);
                    usuario.setId(2L);
                    return usuario;
                });

        UsuarioResponse resultado = servicio.registrar(datos);

        assertEquals("Tecnico Prueba", resultado.nombre());
        assertEquals("TECNICO", resultado.rolNombre());
        assertEquals(1L, resultado.rolId());

        verify(passwordEncoder).encode("clave123");
        verify(repositorio).save(any(Usuario.class));
    }

    @Test
    void rechazarRegistroSinNombre() {
        RegistroRequest datos = new RegistroRequest(
                "",
                "tecnico@induztek.local",
                "clave123",
                "Santiago",
                1L
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.registrar(datos)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarRegistroConContrasenaCorta() {
        RegistroRequest datos = new RegistroRequest(
                "Tecnico",
                "tecnico@induztek.local",
                "123",
                "Santiago",
                1L
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.registrar(datos)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void rechazarCorreoDuplicado() {
        RegistroRequest datos = new RegistroRequest(
                "Tecnico",
                "repetido@induztek.local",
                "clave123",
                "Santiago",
                1L
        );

        when(repositorio.existsByCorreo(datos.correo()))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.registrar(datos)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarRegistroConRolNoPermitido() {
        RegistroRequest datos = new RegistroRequest(
                "Ingeniero",
                "ingeniero@induztek.local",
                "clave123",
                "Santiago",
                2L
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.registrar(datos)
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuarioCorrectamente() {
        Usuario usuario = crearUsuario();

        ActualizarUsuarioRequest datos = new ActualizarUsuarioRequest(
                "Nombre Actualizado",
                "nuevo@induztek.local",
                "Valparaiso",
                null
        );

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repositorio.existsByCorreoAndIdNot(
                "nuevo@induztek.local", 1L
        )).thenReturn(false);

        when(repositorio.save(usuario)).thenReturn(usuario);
        prepararRol();

        UsuarioResponse resultado = servicio.actualizar(1L, datos);

        assertEquals("Nombre Actualizado", resultado.nombre());
        assertEquals("nuevo@induztek.local", resultado.correo());
        assertEquals("Valparaiso", resultado.ciudad());

        verify(repositorio).save(usuario);
    }

    @Test
    void rechazarActualizacionSinNombre() {
        when(repositorio.findById(1L))
                .thenReturn(Optional.of(crearUsuario()));

        ActualizarUsuarioRequest datos = new ActualizarUsuarioRequest(
                "",
                "nuevo@induztek.local",
                "Santiago",
                null
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.actualizar(1L, datos)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void eliminarUsuarioCorrectamente() {
        Usuario usuario = crearUsuario();

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        servicio.eliminar(1L);

        verify(repositorio).delete(usuario);
        verify(almacenamiento).eliminar(usuario.getAvatar());
    }

    @Test
    void rechazarCorreoDuplicadoAlActualizar() {
        Usuario usuario = crearUsuario();

        ActualizarUsuarioRequest datos = new ActualizarUsuarioRequest(
                "Jefe Tecnico",
                "repetido@induztek.local",
                "Santiago",
                null
        );

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repositorio.existsByCorreoAndIdNot(
                "repetido@induztek.local", 1L))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.actualizar(1L, datos)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuarioConRolValido() {
        Usuario usuario = crearUsuario();

        ActualizarUsuarioRequest datos = new ActualizarUsuarioRequest(
                "Jefe Actualizado",
                "jefe@induztek.local",
                "Santiago",
                1L
        );

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(rolClient.obtener(1L))
                .thenReturn(new RolDto(1L, "TECNICO"));

        when(repositorio.save(usuario)).thenReturn(usuario);

        UsuarioResponse resultado = servicio.actualizar(1L, datos);

        assertEquals(1L, resultado.rolId());
        assertEquals("TECNICO", resultado.rolNombre());
        verify(repositorio).save(usuario);
    }

    @Test
    void rechazarActualizacionConRolInexistente() {
        Usuario usuario = crearUsuario();

        ActualizarUsuarioRequest datos = new ActualizarUsuarioRequest(
                "Jefe",
                "jefe@induztek.local",
                "Santiago",
                99L
        );

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.actualizar(1L, datos)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarContrasenaNuevaMuyCorta() {
        Usuario usuario = crearUsuario();

        CambiarContrasenaRequest datos =
                mock(CambiarContrasenaRequest.class);

        when(datos.actual()).thenReturn("anterior123");
        when(datos.nueva()).thenReturn("123");

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "anterior123", "hash-guardado"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cambiarContrasena(1L, datos)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarConsultaPorCorreoInexistente() {
        when(repositorio.findByCorreo("noexiste@induztek.local"))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.obtenerPorCorreo(
                        "noexiste@induztek.local")
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void listarUsuariosSinFiltro() {
        Usuario usuario = crearUsuario();

        when(repositorio.findAllByOrderByNombreAsc())
                .thenReturn(java.util.List.of(usuario));

        when(rolClient.listar()).thenReturn(
                java.util.List.of(
                        new RolDto(3L, "JEFE_TECNICO")
                )
        );

        java.util.List<UsuarioResponse> resultado =
                servicio.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("JEFE_TECNICO", resultado.get(0).rolNombre());
    }

    @Test
    void listarUsuariosConFiltro() {
        Usuario usuario = crearUsuario();

        when(repositorio
                .findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(
                        "Jefe", "Jefe"))
                .thenReturn(java.util.List.of(usuario));

        when(rolClient.listar()).thenReturn(
                java.util.List.of(
                        new RolDto(3L, "JEFE_TECNICO")
                )
        );

        java.util.List<UsuarioResponse> resultado =
                servicio.listar("Jefe");

        assertEquals(1, resultado.size());
        assertEquals("Jefe Tecnico", resultado.get(0).nombre());
    }

    @Test
    void registrarTecnicoConRolPorDefecto() {
        RegistroRequest datos = new RegistroRequest(
                "Tecnico Nuevo",
                "nuevo@induztek.local",
                "clave123",
                "Santiago",
                null
        );

        when(rolClient.obtener(1L))
                .thenReturn(new RolDto(1L, "TECNICO"));

        when(passwordEncoder.encode("clave123"))
                .thenReturn("hash-nuevo");

        when(repositorio.save(any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario usuario = invocacion.getArgument(0);
                    usuario.setId(5L);
                    return usuario;
                });

        UsuarioResponse resultado = servicio.registrar(datos);

        assertEquals(1L, resultado.rolId());
        assertEquals("TECNICO", resultado.rolNombre());
        verify(repositorio).save(any(Usuario.class));
    }

    @Test
    void obtenerAvatarCorrectamente() {
        Usuario usuario = crearUsuario();
        usuario.setAvatar("foto.jpg");

        byte[] imagen = "imagen de prueba".getBytes();

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(almacenamiento.leer("foto.jpg"))
                .thenReturn(imagen);

        byte[] resultado = servicio.obtenerAvatar(1L);

        assertArrayEquals(imagen, resultado);
        verify(almacenamiento).leer("foto.jpg");
    }



}
