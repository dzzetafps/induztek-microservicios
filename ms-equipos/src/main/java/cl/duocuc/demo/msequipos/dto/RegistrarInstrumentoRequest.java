package cl.duocuc.demo.msequipos.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarInstrumentoRequest(

        @NotBlank
        String nombre,

        @NotBlank
        String tipoInstrumento,

        @NotBlank
        String marca,

        String modelo,

        @NotBlank
        String numeroSerie,

        String codigoInterno,

        String fechaVencimientoCalibracion,

        String observaciones
) {
}
