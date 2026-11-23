package cl.duocuc.demo.msvisitas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VisitaTecnicoId implements Serializable {

    @Column(name = "visita_id")
    private Long visitaId;

    @Column(name = "tecnico_id")
    private String tecnicoId;

    public VisitaTecnicoId() {
    }

    public VisitaTecnicoId(Long visitaId, String tecnicoId) {
        this.visitaId = visitaId;
        this.tecnicoId = tecnicoId;
    }

    public Long getVisitaId() {
        return visitaId;
    }

    public void setVisitaId(Long visitaId) {
        this.visitaId = visitaId;
    }

    public String getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(String tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VisitaTecnicoId otro)) return false;
        return Objects.equals(visitaId, otro.visitaId)
                && Objects.equals(tecnicoId, otro.tecnicoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(visitaId, tecnicoId);
    }
}
