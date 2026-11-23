package cl.duocuc.demo.msequipos.service;

import cl.duocuc.demo.msequipos.dto.CalibracionResponse;
import cl.duocuc.demo.msequipos.dto.RegistrarCalibracionRequest;
import cl.duocuc.demo.msequipos.entity.Calibracion;
import cl.duocuc.demo.msequipos.entity.Instrumento;
import cl.duocuc.demo.msequipos.repository.CalibracionRepository;
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

class CalibracionServiceTest {

    private CalibracionRepository calibracionRepository;
    private InstrumentoRepository instrumentoRepository;
    private CalibracionService service;

    @BeforeEach
    void preparar() {
        calibracionRepository = mock(CalibracionRepository.class);
        instrumentoRepository = mock(InstrumentoRepository.class);

        service = new CalibracionService(
                calibracionRepository,
                instrumentoRepository
        );
    }

    private Instrumento instrumentoValido() {
        Instrumento instrumento = new Instrumento();
        instrumento.setId(1L);
        instrumento.setNombre("Multimetro digital");
        return instrumento;
    }

    private RegistrarCalibracionRequest solicitudValida() {
        return new RegistrarCalibracionRequest(
                1L,
                "2026-06-30",
                "2027-06-30"
        );
    }

    private Calibracion calibracionValida() {
        Calibracion calibracion = new Calibracion();
        calibracion.setId(1L);
        calibracion.setInstrumento(instrumentoValido());
        calibracion.setFechaCalibracion("2026-06-30");
        calibracion.setFechaVencimiento("2027-06-30");
        return calibracion;
    }

    @Test
    void registrarCalibracionCorrectamente() {
        when(instrumentoRepository.findById(1L))
                .thenReturn(Optional.of(instrumentoValido()));

        when(calibracionRepository.save(any(Calibracion.class)))
                .thenAnswer(invocacion -> {
                    Calibracion calibracion =
                            invocacion.getArgument(0);
                    calibracion.setId(1L);
                    return calibracion;
                });

        CalibracionResponse resultado =
                service.registrarCalibracion(solicitudValida());

        assertEquals(1L, resultado.id());
        assertEquals(1L, resultado.instrumentoId());
        assertEquals("2026-06-30", resultado.fechaCalibracion());
        assertEquals("2027-06-30", resultado.fechaVencimiento());

        verify(calibracionRepository).save(any(Calibracion.class));
    }

    @Test
    void rechazarSolicitudNula() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarCalibracion(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(calibracionRepository);
    }

    @Test
    void rechazarInstrumentoIdInvalido() {
        RegistrarCalibracionRequest request =
                new RegistrarCalibracionRequest(
                        0L, "2026-06-30", "2027-06-30"
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarCalibracion(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(instrumentoRepository);
    }

    @Test
    void rechazarInstrumentoInexistente() {
        when(instrumentoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RegistrarCalibracionRequest request =
                new RegistrarCalibracionRequest(
                        99L, "2026-06-30", "2027-06-30"
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarCalibracion(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(calibracionRepository, never())
                .save(any(Calibracion.class));
    }

    @Test
    void rechazarFechaInvalida() {
        when(instrumentoRepository.findById(1L))
                .thenReturn(Optional.of(instrumentoValido()));

        RegistrarCalibracionRequest request =
                new RegistrarCalibracionRequest(
                        1L, "fecha-invalida", "2027-06-30"
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarCalibracion(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void rechazarVencimientoAnteriorACalibracion() {
        when(instrumentoRepository.findById(1L))
                .thenReturn(Optional.of(instrumentoValido()));

        RegistrarCalibracionRequest request =
                new RegistrarCalibracionRequest(
                        1L, "2027-06-30", "2026-06-30"
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarCalibracion(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(calibracionRepository, never())
                .save(any(Calibracion.class));
    }

    @Test
    void rechazarFechaVacia() {
        RegistrarCalibracionRequest request =
                new RegistrarCalibracionRequest(
                        1L, "", "2027-06-30"
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarCalibracion(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void listarTodasLasCalibraciones() {
        when(calibracionRepository.findAll())
                .thenReturn(List.of(calibracionValida()));

        List<CalibracionResponse> resultado =
                service.listarCalibraciones(null);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).instrumentoId());
        verify(calibracionRepository).findAll();
    }

    @Test
    void listarCalibracionesPorInstrumento() {
        when(calibracionRepository.findByInstrumento_Id(1L))
                .thenReturn(List.of(calibracionValida()));

        List<CalibracionResponse> resultado =
                service.listarCalibraciones(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).instrumentoId());

        verify(calibracionRepository)
                .findByInstrumento_Id(1L);
    }

    @Test
    void obtenerCalibracionExistente() {
        when(calibracionRepository.findById(1L))
                .thenReturn(Optional.of(calibracionValida()));

        CalibracionResponse resultado =
                service.obtenerCalibracion(1L);

        assertEquals(1L, resultado.id());
        assertEquals("2027-06-30", resultado.fechaVencimiento());
    }

    @Test
    void calibracionInexistenteDevuelve404() {
        when(calibracionRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerCalibracion(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
