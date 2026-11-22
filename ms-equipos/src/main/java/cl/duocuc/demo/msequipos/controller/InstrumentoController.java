package cl.duocuc.demo.msequipos.controller;

import cl.duocuc.demo.msequipos.dto.RegistrarInstrumentoRequest;
import cl.duocuc.demo.msequipos.entity.Instrumento;
import cl.duocuc.demo.msequipos.service.InstrumentoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    private final InstrumentoService instrumentoService;

    public InstrumentoController(
            InstrumentoService instrumentoService) {
        this.instrumentoService = instrumentoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Instrumento registrarInstrumento(
            @Valid @RequestBody RegistrarInstrumentoRequest request) {
        return instrumentoService.registrarInstrumento(request);
    }

    @GetMapping
    public List<Instrumento> listarInstrumentos() {
        return instrumentoService.listarInstrumentos();
    }

    @GetMapping("/{id}")
    public Instrumento obtenerInstrumento(@PathVariable Long id) {
        return instrumentoService.obtenerInstrumento(id);
    }
}
