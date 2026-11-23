package cl.duocuc.demo.msvisitas.service;

import cl.duocuc.demo.msvisitas.dto.RegistrarVisitaRequest;
import cl.duocuc.demo.msvisitas.entity.Visita;
import cl.duocuc.demo.msvisitas.entity.VisitaTecnico;
import cl.duocuc.demo.msvisitas.repository.VisitaRepository;
import cl.duocuc.demo.msvisitas.repository.VisitaTecnicoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitaServiceTest {

    @Mock
    private VisitaRepository visitaRepository;

    @Mock
    private VisitaTecnicoRepository tecnicoRepository;

    @InjectMocks
    private VisitaService service;

    private static final Long FECHA = 1795305600000L;

    private static RegistrarVisitaRequest solicitud(
            String cliente,
            String instalacion,
            Long fecha,
            String ot,
            String tipo,
            String ingeniero,
            List<String> tecnicos) {

        return new RegistrarVisitaRequest(
                cliente,
                instalacion,
                fecha,
                ot,
                tipo,
                ingeniero,
                "Visita de prueba",
                tecnicos
        );
    }

    private static RegistrarVisitaRequest valida() {
        return solicitud(
                "CLI-01",
                "INS-01",
                FECHA,
                "OT-001",
                "SOLO_PRUEBAS",
                "ING-01",
                List.of("TEC-01", "TEC-02")
        );
    }

    private void prepararGuardado() {
        when(visitaRepository.save(any(Visita.class)))
                .thenAnswer(invocacion -> {
                    Visita visita = invocacion.getArgument(0);
                    visita.setId(1L);
                    return visita;
                });
    }

    @Test
    void registraVisitaConTecnicos() {
        prepararGuardado();

        Visita resultado = service.registrarVisita(valida());

        assertEquals(1L, resultado.getId());
        assertEquals("CLI-01", resultado.getClienteId());
        assertEquals("INS-01", resultado.getInstalacionId());
        assertEquals(FECHA, resultado.getFechaMillis());
        assertEquals("OT-001", resultado.getNumeroOt());
        assertEquals("SOLO_PRUEBAS", resultado.getTipoVisita());
        assertEquals("ING-01", resultado.getIngenieroCargoId());
        assertEquals("Visita de prueba", resultado.getObservaciones());

        verify(tecnicoRepository).saveAll(argThat(items -> {
            List<VisitaTecnico> lista = new ArrayList<>();
            items.forEach(lista::add);

            return lista.size() == 2
                    && lista.get(0).getId().getVisitaId().equals(1L)
                    && lista.get(0).getId().getTecnicoId().equals("TEC-01")
                    && lista.get(1).getId().getTecnicoId().equals("TEC-02");
        }));
    }

    @Test
    void aceptaVisitaDePruebasYMantenimiento() {
        prepararGuardado();

        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-002", "PRUEBAS_Y_MANT",
                "ING-01", List.of("TEC-01")
        );

        Visita resultado = service.registrarVisita(request);

        assertEquals("PRUEBAS_Y_MANT", resultado.getTipoVisita());
    }

    @Test
    void registraSinTecnicosCuandoListaEsNula() {
        prepararGuardado();

        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-003", "SOLO_PRUEBAS",
                "ING-01", null
        );

        assertNotNull(service.registrarVisita(request));

        verify(tecnicoRepository).saveAll(
                argThat(items -> !items.iterator().hasNext())
        );
    }

    @Test
    void registraSinTecnicosCuandoListaEstaVacia() {
        prepararGuardado();

        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-004", "SOLO_PRUEBAS",
                "ING-01", List.of()
        );

        assertNotNull(service.registrarVisita(request));

        verify(tecnicoRepository).saveAll(
                argThat(items -> !items.iterator().hasNext())
        );
    }

    @Test
    void eliminaEspaciosDelNumeroOt() {
        prepararGuardado();

        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                " OT-005 ", "SOLO_PRUEBAS",
                "ING-01", List.of()
        );

        Visita resultado = service.registrarVisita(request);

        assertEquals("OT-005", resultado.getNumeroOt());

        verify(visitaRepository).existsByNumeroOt("OT-005");
    }

    @Test
    void rechazaNumeroOtDuplicado() {
        when(visitaRepository.existsByNumeroOt("OT-001"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarVisita(valida())
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());

        verify(visitaRepository, never()).save(any());
        verifyNoInteractions(tecnicoRepository);
    }

    @Test
    void rechazaTipoVisitaNoValido() {
        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-001", "TIPO_DESCONOCIDO",
                "ING-01", List.of("TEC-01")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarVisita(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(visitaRepository, tecnicoRepository);
    }

    @Test
    void rechazaTecnicoNulo() {
        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-001", "SOLO_PRUEBAS",
                "ING-01", Arrays.asList("TEC-01", null)
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarVisita(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(visitaRepository, never()).save(any());
    }

    @Test
    void rechazaTecnicoVacio() {
        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-001", "SOLO_PRUEBAS",
                "ING-01", List.of(" ")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarVisita(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(visitaRepository, never()).save(any());
    }

    @Test
    void rechazaTecnicosDuplicados() {
        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-001", "SOLO_PRUEBAS",
                "ING-01", List.of("TEC-01", "TEC-01")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarVisita(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(visitaRepository, never()).save(any());
    }

    @Test
    void rechazaTecnicosDuplicadosConEspacios() {
        RegistrarVisitaRequest request = solicitud(
                "CLI-01", "INS-01", FECHA,
                "OT-001", "SOLO_PRUEBAS",
                "ING-01", List.of("TEC-01", " TEC-01 ")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarVisita(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(visitaRepository, never()).save(any());
    }

    @Test
    void listaVisitasPorFechaDescendente() {
        Visita visita = new Visita();
        visita.setId(1L);

        when(visitaRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(visita));

        List<Visita> resultado = service.listarVisitas();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());

        verify(visitaRepository).findAll(
                Sort.by(Sort.Direction.DESC, "fechaMillis")
        );
    }

    @Test
    void obtieneVisitaExistente() {
        Visita visita = new Visita();
        visita.setId(5L);

        when(visitaRepository.findById(5L))
                .thenReturn(Optional.of(visita));

        Visita resultado = service.obtenerVisita(5L);

        assertEquals(5L, resultado.getId());
    }

    @Test
    void devuelve404CuandoVisitaNoExiste() {
        when(visitaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerVisita(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    static Stream<Arguments> solicitudesInvalidas() {
        return Stream.of(
            Arguments.of((Object) null),
            Arguments.of(solicitud(
                null, "INS-01", FECHA, "OT-001",
                "SOLO_PRUEBAS", "ING-01", List.of())),
            Arguments.of(solicitud(
                " ", "INS-01", FECHA, "OT-001",
                "SOLO_PRUEBAS", "ING-01", List.of())),
            Arguments.of(solicitud(
                "CLI-01", null, FECHA, "OT-001",
                "SOLO_PRUEBAS", "ING-01", List.of())),
            Arguments.of(solicitud(
                "CLI-01", " ", FECHA, "OT-001",
                "SOLO_PRUEBAS", "ING-01", List.of())),
            Arguments.of(solicitud(
                "CLI-01", "INS-01", null, "OT-001",
                "SOLO_PRUEBAS", "ING-01", List.of())),
            Arguments.of(solicitud(
                "CLI-01", "INS-01", FECHA, null,
                "SOLO_PRUEBAS", "ING-01", List.of())),
            Arguments.of(solicitud(
                "CLI-01", "INS-01", FECHA, " ",
                "SOLO_PRUEBAS", "ING-01", List.of())),
            Arguments.of(solicitud(
                "CLI-01", "INS-01", FECHA, "OT-001",
                null, "ING-01", List.of())),
            Arguments.of(solicitud(
                "CLI-01", "INS-01", FECHA, "OT-001",
                "SOLO_PRUEBAS", null, List.of())),
            Arguments.of(solicitud(
                "CLI-01", "INS-01", FECHA, "OT-001",
                "SOLO_PRUEBAS", " ", List.of()))
        );
    }

    @ParameterizedTest
    @MethodSource("solicitudesInvalidas")
    void rechazaDatosObligatoriosIncompletos(
            RegistrarVisitaRequest request) {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarVisita(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());

        verifyNoInteractions(visitaRepository, tecnicoRepository);
    }
}
