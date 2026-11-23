package cl.duocuc.demo.msvisitas.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisitaTecnicoIdTest {

    @Test
    void comparaIdentificadoresIguales() {
        VisitaTecnicoId primero =
                new VisitaTecnicoId(1L, "TEC-01");

        VisitaTecnicoId segundo =
                new VisitaTecnicoId(1L, "TEC-01");

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
        assertEquals(primero, primero);
    }

    @Test
    void identificaVisitasDiferentes() {
        VisitaTecnicoId primero =
                new VisitaTecnicoId(1L, "TEC-01");

        VisitaTecnicoId segundo =
                new VisitaTecnicoId(2L, "TEC-01");

        assertNotEquals(primero, segundo);
    }

    @Test
    void identificaTecnicosDiferentes() {
        VisitaTecnicoId primero =
                new VisitaTecnicoId(1L, "TEC-01");

        VisitaTecnicoId segundo =
                new VisitaTecnicoId(1L, "TEC-02");

        assertNotEquals(primero, segundo);
    }

    @Test
    void noEsIgualANuloNiAOtroTipo() {
        VisitaTecnicoId id =
                new VisitaTecnicoId(1L, "TEC-01");

        assertNotEquals(null, id);
        assertNotEquals("TEC-01", id);
    }

    @Test
    void permiteModificarSusCampos() {
        VisitaTecnicoId id = new VisitaTecnicoId();

        id.setVisitaId(10L);
        id.setTecnicoId("TEC-10");

        assertEquals(10L, id.getVisitaId());
        assertEquals("TEC-10", id.getTecnicoId());
    }

    @Test
    void permiteCrearRelacionConVisita() {
        Visita visita = new Visita();
        visita.setId(5L);

        VisitaTecnico asociacion =
                new VisitaTecnico(visita, "TEC-05");

        assertSame(visita, asociacion.getVisita());
        assertEquals(5L, asociacion.getId().getVisitaId());
        assertEquals("TEC-05", asociacion.getId().getTecnicoId());
    }

    @Test
    void permiteAsignarRelacionManualmente() {
        Visita visita = new Visita();
        visita.setId(6L);

        VisitaTecnicoId id =
                new VisitaTecnicoId(6L, "TEC-06");

        VisitaTecnico asociacion = new VisitaTecnico();
        asociacion.setVisita(visita);
        asociacion.setId(id);

        assertSame(visita, asociacion.getVisita());
        assertSame(id, asociacion.getId());
    }
}
