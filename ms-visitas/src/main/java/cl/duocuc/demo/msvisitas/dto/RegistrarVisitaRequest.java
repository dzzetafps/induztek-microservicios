package cl.duocuc.demo.msvisitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RegistrarVisitaRequest(

        @NotBlank
        String clienteId,

        @NotBlank
        String instalacionId,

        @NotNull
        Long fechaMillis,

        @NotBlank
        String numeroOt,

        @NotBlank
        String tipoVisita,

        @NotBlank
        String ingenieroCargoId,

        String observaciones,

        List<String> tecnicoIds
) {
}
