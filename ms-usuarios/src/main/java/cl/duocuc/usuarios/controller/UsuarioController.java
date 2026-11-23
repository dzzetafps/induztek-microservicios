
package cl.duocuc.usuarios.controller;

import cl.duocuc.usuarios.dto.*;
import cl.duocuc.usuarios.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar(
            @RequestParam(required = false) String buscar,
            Authentication auth) {

        exigirJefe(auth);
        return usuarioService.listar(buscar);
    }

    @GetMapping("/me")
    public UsuarioResponse miPerfil(Authentication auth) {
        return usuarioService.obtenerPorCorreo(auth.getName());
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(
            @PathVariable Long id,
            Authentication auth) {

        exigirPropietarioOJefe(id, auth);
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(
            @RequestBody RegistroRequest datos,
            Authentication auth) {

        exigirJefe(auth);
        return usuarioService.registrar(datos);
    }

    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(
            @RequestBody LoginRequest datos) {

        return usuarioService.iniciarSesion(datos);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarUsuarioRequest datos,
            Authentication auth) {

        exigirPropietarioOJefe(id, auth);

        UsuarioResponse actual = usuarioService.obtener(id);

        if (datos.rolId() != null
                && !datos.rolId().equals(actual.rolId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se permite modificar el rol");
        }

        return usuarioService.actualizar(id, datos);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(
            @PathVariable Long id,
            @RequestBody CambiarContrasenaRequest datos,
            Authentication auth) {

        exigirPropietario(id, auth);
        usuarioService.cambiarContrasena(id, datos);
    }

    @PostMapping(
            value = "/{id}/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse subirAvatar(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            Authentication auth) {

        exigirPropietario(id, auth);
        return usuarioService.subirAvatar(id, archivo);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> obtenerAvatar(
            @PathVariable Long id,
            Authentication auth) {

        exigirPropietarioOJefe(id, auth);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(usuarioService.obtenerAvatar(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id,
            Authentication auth) {

        exigirJefe(auth);
        usuarioService.eliminar(id);
    }

    private boolean esJefe(Authentication auth) {
        return auth != null
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_JEFE_TECNICO"));
    }

    private boolean esPropietario(
            Long id, Authentication auth) {

        if (auth == null) {
            return false;
        }

        UsuarioResponse usuario = usuarioService.obtener(id);

        return usuario.correo()
                .equalsIgnoreCase(auth.getName());
    }

    private void exigirJefe(Authentication auth) {
        if (!esJefe(auth)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso exclusivo del jefe tecnico");
        }
    }

    private void exigirPropietario(
            Long id, Authentication auth) {

        if (!esPropietario(id, auth)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo puedes modificar tu propia cuenta");
        }
    }

    private void exigirPropietarioOJefe(
            Long id, Authentication auth) {

        if (!esJefe(auth) && !esPropietario(id, auth)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder");
        }
    }
}
