
package cl.duocuc.usuarios.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AlmacenamientoServiceTest {

    @TempDir
    Path carpetaTemporal;

    private AlmacenamientoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AlmacenamientoService(
                carpetaTemporal.toString()
        );
    }

    @Test
    void guardarImagenCorrectamente() throws Exception {
        MockMultipartFile imagen = new MockMultipartFile(
                "archivo",
                "foto.jpg",
                "image/jpeg",
                "imagen de prueba".getBytes()
        );

        String nombre = servicio.guardar(imagen);

        assertNotNull(nombre);
        assertTrue(nombre.endsWith(".jpg"));
        assertTrue(Files.exists(carpetaTemporal.resolve(nombre)));
    }

    @Test
    void leerImagenCorrectamente() throws Exception {
        byte[] contenido = "imagen de prueba".getBytes();

        MockMultipartFile imagen = new MockMultipartFile(
                "archivo",
                "foto.jpg",
                "image/jpeg",
                contenido
        );

        String nombre = servicio.guardar(imagen);
        byte[] resultado = servicio.leer(nombre);

        assertArrayEquals(contenido, resultado);
    }

    @Test
    void rechazarImagenVacia() {
        MockMultipartFile imagen = new MockMultipartFile(
                "archivo",
                "foto.jpg",
                "image/jpeg",
                new byte[0]
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.guardar(imagen)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void rechazarArchivoQueNoEsImagen() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "documento.txt",
                "text/plain",
                "contenido".getBytes()
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.guardar(archivo)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void rechazarArchivoSinTipo() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "archivo",
                null,
                "contenido".getBytes()
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.guardar(archivo)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void devolverErrorCuandoImagenNoExiste() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.leer("inexistente.jpg")
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void eliminarImagenCorrectamente() throws Exception {
        MockMultipartFile imagen = new MockMultipartFile(
                "archivo",
                "foto.jpg",
                "image/jpeg",
                "contenido".getBytes()
        );

        String nombre = servicio.guardar(imagen);

        servicio.eliminar(nombre);

        assertFalse(Files.exists(carpetaTemporal.resolve(nombre)));
    }

    @Test
    void eliminarImagenNulaNoProduceError() {
        assertDoesNotThrow(() -> servicio.eliminar(null));
    }

    @Test
    void eliminarImagenInexistenteNoProduceError() {
        assertDoesNotThrow(
                () -> servicio.eliminar("inexistente.jpg")
        );
    }
}
