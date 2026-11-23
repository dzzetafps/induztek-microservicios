package cl.duocuc.demo.msequipos.dto;

public record CalibracionResponse(
        Long id,
        Long instrumentoId,
        String fechaCalibracion,
        String fechaVencimiento
) {
}
