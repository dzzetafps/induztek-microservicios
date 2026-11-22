package cl.duocuc.demo.msvisitas.controller;

import cl.duocuc.demo.msvisitas.dto.RegistrarVisitaRequest;
import cl.duocuc.demo.msvisitas.entity.Visita;
import cl.duocuc.demo.msvisitas.service.VisitaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitas")
public class VisitaController {

    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Visita registrarVisita(
            @Valid @RequestBody RegistrarVisitaRequest request) {
        return visitaService.registrarVisita(request);
    }

    @GetMapping
    public List<Visita> listarVisitas() {
        return visitaService.listarVisitas();
    }

    @GetMapping("/{id}")
    public Visita obtenerVisita(@PathVariable Long id) {
        return visitaService.obtenerVisita(id);
    }
}
