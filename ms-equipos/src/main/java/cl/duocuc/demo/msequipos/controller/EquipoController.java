package cl.duocuc.demo.msequipos.controller;

import cl.duocuc.demo.msequipos.dto.RegistrarEquipoRequest;
import cl.duocuc.demo.msequipos.entity.Equipo;
import cl.duocuc.demo.msequipos.service.EquipoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipo registrarEquipo(
            @Valid @RequestBody RegistrarEquipoRequest request) {
        return equipoService.registrarEquipo(request);
    }

    @GetMapping
    public List<Equipo> listarEquipos(
            @RequestParam(required = false) String clienteId,
            @RequestParam(required = false) String instalacionId) {
        return equipoService.listarEquipos(clienteId, instalacionId);
    }

    @GetMapping("/{id}")
    public Equipo obtenerEquipo(@PathVariable Long id) {
        return equipoService.obtenerEquipo(id);
    }
}
