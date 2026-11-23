package cl.duocuc.demo.msequipos.service;

import cl.duocuc.demo.msequipos.dto.RegistrarInstrumentoRequest;
import cl.duocuc.demo.msequipos.entity.Instrumento;
import cl.duocuc.demo.msequipos.repository.InstrumentoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InstrumentoServiceTest {

    private InstrumentoRepository repository;
    private InstrumentoService service;

    @BeforeEach
    void preparar() {
        repository = mock(InstrumentoRepository.class);
        service = new InstrumentoService(repository);
    }

    private RegistrarInstrumentoRequest solicitudValida() {
        return new RegistrarInstrumentoRequest(
                "Multimetro digital",
                "Multimetro",
                "Fluke",
                "87V",
                "SER-INST-002",
                "INST-002",
                "2027-06-30",
                "Instrumento de prueba"
        );
    }

    @Test
    void registrarInstrumentoCorrectamente() {
        when(repository.save(any(Instrumento.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Instrumento resultado =
                service.registrarInstrumento(solicitudValida());

        assertEquals("Fluke", resultado.getMarca());
        assertEquals("SER-INST-002", resultado.getNumeroSerie());
        assertEquals("2027-06-30",
                resultado.getFechaVencimientoCalibracion());

        verify(repository).save(any(Instrumento.class));
    }

    @Test
    void rechazarNumeroSerieDuplicado() {
        when(repository.existsByNumeroSerie("SER-INST-002"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarInstrumento(solicitudValida())
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any(Instrumento.class));
    }

    @Test
    void rechazarNombreVacio() {
        RegistrarInstrumentoRequest request =
                new RegistrarInstrumentoRequest(
                        "",
                        "Multimetro",
                        "Fluke",
                        "87V",
                        "SER-INST-003",
                        "INST-003",
                        "2027-06-30",
                        null
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarInstrumento(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void rechazarFechaInvalida() {
        RegistrarInstrumentoRequest request =
                new RegistrarInstrumentoRequest(
                        "Multimetro digital",
                        "Multimetro",
                        "Fluke",
                        "87V",
                        "SER-INST-004",
                        "INST-004",
                        "fecha-invalida",
                        null
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarInstrumento(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(repository, never()).save(any(Instrumento.class));
    }

    @Test
    void permitirInstrumentoSinFechaCalibracion() {
        RegistrarInstrumentoRequest request =
                new RegistrarInstrumentoRequest(
                        "Multimetro digital",
                        "Multimetro",
                        "Fluke",
                        "87V",
                        "SER-INST-005",
                        "INST-005",
                        null,
                        null
                );

        when(repository.save(any(Instrumento.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Instrumento resultado =
                service.registrarInstrumento(request);

        assertNull(resultado.getFechaVencimientoCalibracion());
        verify(repository).save(any(Instrumento.class));
    }

    @Test
    void rechazarSolicitudNula() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarInstrumento(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void listarInstrumentosCorrectamente() {
        when(repository.findAll())
                .thenReturn(List.of(new Instrumento()));

        List<Instrumento> resultado =
                service.listarInstrumentos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void obtenerInstrumentoExistente() {
        Instrumento instrumento = new Instrumento();
        instrumento.setId(1L);
        instrumento.setNombre("Multimetro digital");

        when(repository.findById(1L))
                .thenReturn(Optional.of(instrumento));

        Instrumento resultado = service.obtenerInstrumento(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Multimetro digital", resultado.getNombre());
    }

    @Test
    void instrumentoInexistenteDevuelve404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerInstrumento(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}