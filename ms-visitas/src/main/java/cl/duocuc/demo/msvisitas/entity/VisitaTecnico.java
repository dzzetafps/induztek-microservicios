package cl.duocuc.demo.msvisitas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "visita_tecnicos")
public class VisitaTecnico {

    @EmbeddedId
    private VisitaTecnicoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("visitaId")
    @JoinColumn(name = "visita_id", nullable = false)
    private Visita visita;

    public VisitaTecnico() {
    }

    public VisitaTecnico(Visita visita, String tecnicoId) {
        this.visita = visita;
        this.id = new VisitaTecnicoId(visita.getId(), tecnicoId);
    }

    public VisitaTecnicoId getId() {
        return id;
    }

    public void setId(VisitaTecnicoId id) {
        this.id = id;
    }

    public Visita getVisita() {
        return visita;
    }

    public void setVisita(Visita visita) {
        this.visita = visita;
    }
}
