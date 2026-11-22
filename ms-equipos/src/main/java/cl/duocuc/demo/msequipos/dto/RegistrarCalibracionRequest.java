package cl.duocuc.demo.msequipos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarCalibracionRequest(

        @NotNull
        Long instrumentoId,

        @NotBlank
        String fechaCalibracion,

        @NotBlank
        String fechaVencimiento
) {
}
