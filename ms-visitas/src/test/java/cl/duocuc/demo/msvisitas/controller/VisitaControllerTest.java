package cl.duocuc.demo.msvisitas.controller;

import cl.duocuc.demo.msvisitas.dto.RegistrarVisitaRequest;
import cl.duocuc.demo.msvisitas.entity.Visita;
import cl.duocuc.demo.msvisitas.service.VisitaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitaControllerTest {

    @Mock
    private VisitaService service;

    private VisitaController controller;

    @BeforeEach
    void configurar() {
        controller = new VisitaController(service);
    }

    @Test
    void registraVisita() {
        RegistrarVisitaRequest request = new RegistrarVisitaRequest(
                "CLI-01",
                "INS-01",
                1795305600000L,
                "OT-001",
                "SOLO_PRUEBAS",
                "ING-01",
                "Prueba",
                List.of("TEC-01")
        );

        Visita visita = new Visita();
        visita.setId(1L);

        when(service.registrarVisita(request)).thenReturn(visita);

        Visita resultado = controller.registrarVisita(request);

        assertSame(visita, resultado);
        verify(service).registrarVisita(request);
    }

    @Test
    void listaVisitas() {
        Visita visita = new Visita();
        visita.setId(2L);

        when(service.listarVisitas())
                .thenReturn(List.of(visita));

        List<Visita> resultado = controller.listarVisitas();

        assertEquals(1, resultado.size());
        assertSame(visita, resultado.get(0));
        verify(service).listarVisitas();
    }

    @Test
    void obtieneVisitaPorId() {
        Visita visita = new Visita();
        visita.setId(3L);

        when(service.obtenerVisita(3L)).thenReturn(visita);

        Visita resultado = controller.obtenerVisita(3L);

        assertSame(visita, resultado);
        verify(service).obtenerVisita(3L);
    }
}
