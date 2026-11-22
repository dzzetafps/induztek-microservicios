package cl.duocuc.demo.msvisitas.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "visitas",
    indexes = {
        @Index(name = "idx_visita_cliente", columnList = "clienteId"),
        @Index(name = "idx_visita_instalacion", columnList = "instalacionId")
    }
)
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clienteId;
    private String instalacionId;
    private Long fechaMillis;

    @Column(nullable = false, unique = true)
    private String numeroOt;

    private String tipoVisita;
    private String ingenieroCargoId;

    @Column(length = 2000)
    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getInstalacionId() {
        return instalacionId;
    }

    public void setInstalacionId(String instalacionId) {
        this.instalacionId = instalacionId;
    }

    public Long getFechaMillis() {
        return fechaMillis;
    }

    public void setFechaMillis(Long fechaMillis) {
        this.fechaMillis = fechaMillis;
    }

    public String getNumeroOt() {
        return numeroOt;
    }

    public void setNumeroOt(String numeroOt) {
        this.numeroOt = numeroOt;
    }

    public String getTipoVisita() {
        return tipoVisita;
    }

    public void setTipoVisita(String tipoVisita) {
        this.tipoVisita = tipoVisita;
    }

    public String getIngenieroCargoId() {
        return ingenieroCargoId;
    }

    public void setIngenieroCargoId(String ingenieroCargoId) {
        this.ingenieroCargoId = ingenieroCargoId;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
