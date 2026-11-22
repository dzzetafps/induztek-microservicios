package cl.duocuc.demo.msequipos.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarEquipoRequest(

        @NotBlank
        String clienteId,

        @NotBlank
        String instalacionId,

        @NotBlank
        String nombre,

        @NotBlank
        String tipo,

        @NotBlank
        String marca,

        String modelo,

        @NotBlank
        String numeroSerie,

        String codigoInterno,

        String observaciones
) {
}
