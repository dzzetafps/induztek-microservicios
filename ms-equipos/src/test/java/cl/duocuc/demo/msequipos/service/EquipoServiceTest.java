package cl.duocuc.demo.msequipos.service;

import cl.duocuc.demo.msequipos.dto.RegistrarEquipoRequest;
import cl.duocuc.demo.msequipos.entity.Equipo;
import cl.duocuc.demo.msequipos.repository.EquipoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EquipoServiceTest {

    private EquipoRepository repository;
    private EquipoService service;

    @BeforeEach
    void preparar() {
        repository = mock(EquipoRepository.class);
        service = new EquipoService(repository);
    }

    private RegistrarEquipoRequest solicitudValida() {
        return new RegistrarEquipoRequest(
                "CLI-01",
                "INS-01",
                "Motor electrico",
                "Motor",
                "WEG",
                "W22",
                "SER-002",
                "EQ-002",
                "Prueba unitaria"
        );
    }

    @Test
    void registrarEquipoCorrectamente() {
        when(repository.save(any(Equipo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Equipo resultado = service.registrarEquipo(
                solicitudValida());

        assertEquals("SER-002", resultado.getNumeroSerie());
        assertEquals("WEG", resultado.getMarca());
        verify(repository).save(any(Equipo.class));
    }

    @Test
    void rechazarNumeroSerieDuplicado() {
        when(repository.existsByNumeroSerie("SER-002"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarEquipo(solicitudValida())
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any(Equipo.class));
    }

    @Test
    void rechazarNombreVacio() {
        RegistrarEquipoRequest request = new RegistrarEquipoRequest(
                "CLI-01", "INS-01", "",
                "Motor", "WEG", "W22",
                "SER-002", "EQ-002", null
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarEquipo(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void listarEquiposPorClienteEInstalacion() {
        when(repository.findByClienteIdAndInstalacionId(
                "CLI-01", "INS-01"))
                .thenReturn(List.of(new Equipo()));

        List<Equipo> resultado = service.listarEquipos(
                "CLI-01", "INS-01");

        assertEquals(1, resultado.size());
        verify(repository).findByClienteIdAndInstalacionId(
                "CLI-01", "INS-01");
    }

    @Test
    void rechazarInstalacionSinCliente() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.listarEquipos(null, "INS-01")
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void equipoInexistenteDevuelve404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerEquipo(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
