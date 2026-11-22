package cl.duocuc.demo.msequipos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "calibraciones")
public class Calibracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "instrumento_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_calibracion_instrumento"
        )
    )
    private Instrumento instrumento;

    @Column(nullable = false)
    private String fechaCalibracion;

    @Column(nullable = false)
    private String fechaVencimiento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instrumento getInstrumento() {
        return instrumento;
    }

    public void setInstrumento(Instrumento instrumento) {
        this.instrumento = instrumento;
    }

    public String getFechaCalibracion() {
        return fechaCalibracion;
    }

    public void setFechaCalibracion(String fechaCalibracion) {
        this.fechaCalibracion = fechaCalibracion;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
