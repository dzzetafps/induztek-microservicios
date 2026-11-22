package cl.duocuc.demo.msequipos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "instrumentos_medicion")
public class Instrumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipoInstrumento;

    @Column(nullable = false)
    private String marca;

    private String modelo;

    @Column(nullable = false, unique = true)
    private String numeroSerie;

    private String codigoInterno;

    private String fechaVencimientoCalibracion;

    @Column(length = 2000)
    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoInstrumento() {
        return tipoInstrumento;
    }

    public void setTipoInstrumento(String tipoInstrumento) {
        this.tipoInstrumento = tipoInstrumento;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public String getFechaVencimientoCalibracion() {
        return fechaVencimientoCalibracion;
    }

    public void setFechaVencimientoCalibracion(
            String fechaVencimientoCalibracion) {
        this.fechaVencimientoCalibracion =
                fechaVencimientoCalibracion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
