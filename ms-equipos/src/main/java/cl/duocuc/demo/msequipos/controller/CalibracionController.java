package cl.duocuc.demo.msequipos.controller;

import cl.duocuc.demo.msequipos.dto.CalibracionResponse;
import cl.duocuc.demo.msequipos.dto.RegistrarCalibracionRequest;
import cl.duocuc.demo.msequipos.service.CalibracionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calibraciones")
public class CalibracionController {

    private final CalibracionService calibracionService;

    public CalibracionController(
            CalibracionService calibracionService) {
        this.calibracionService = calibracionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CalibracionResponse registrarCalibracion(
            @Valid @RequestBody RegistrarCalibracionRequest request) {
        return calibracionService.registrarCalibracion(request);
    }

    @GetMapping
    public List<CalibracionResponse> listarCalibraciones(
            @RequestParam(required = false) Long instrumentoId) {
        return calibracionService.listarCalibraciones(instrumentoId);
    }

    @GetMapping("/{id}")
    public CalibracionResponse obtenerCalibracion(
            @PathVariable Long id) {
        return calibracionService.obtenerCalibracion(id);
    }
}
