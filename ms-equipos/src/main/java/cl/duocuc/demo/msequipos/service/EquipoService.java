package cl.duocuc.demo.msequipos.service;

import cl.duocuc.demo.msequipos.dto.RegistrarEquipoRequest;
import cl.duocuc.demo.msequipos.entity.Equipo;
import cl.duocuc.demo.msequipos.repository.EquipoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;

    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    @Transactional
    public Equipo registrarEquipo(RegistrarEquipoRequest request) {

        if (request == null
                || request.clienteId() == null
                || request.clienteId().isBlank()
                || request.instalacionId() == null
                || request.instalacionId().isBlank()
                || request.nombre() == null
                || request.nombre().isBlank()
                || request.tipo() == null
                || request.tipo().isBlank()
                || request.marca() == null
                || request.marca().isBlank()
                || request.numeroSerie() == null
                || request.numeroSerie().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos obligatorios incompletos");
        }

        String numeroSerie = request.numeroSerie().trim();

        if (equipoRepository.existsByNumeroSerie(numeroSerie)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un equipo con este numero de serie");
        }

        Equipo equipo = new Equipo();

        equipo.setClienteId(request.clienteId().trim());
        equipo.setInstalacionId(request.instalacionId().trim());
        equipo.setNombre(request.nombre().trim());
        equipo.setTipo(request.tipo().trim());
        equipo.setMarca(request.marca().trim());
        equipo.setModelo(request.modelo());
        equipo.setNumeroSerie(numeroSerie);
        equipo.setCodigoInterno(request.codigoInterno());
        equipo.setObservaciones(request.observaciones());

        return equipoRepository.save(equipo);
    }

    @Transactional(readOnly = true)
    public List<Equipo> listarEquipos(
            String clienteId,
            String instalacionId) {

        if (clienteId == null || clienteId.isBlank()) {
            if (instalacionId != null && !instalacionId.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debe indicar un cliente para filtrar por instalacion");
            }
            return equipoRepository.findAll();
        }

        if (instalacionId != null && !instalacionId.isBlank()) {
            return equipoRepository
                    .findByClienteIdAndInstalacionId(
                            clienteId.trim(),
                            instalacionId.trim());
        }

        return equipoRepository.findByClienteId(clienteId.trim());
    }

    @Transactional(readOnly = true)
    public Equipo obtenerEquipo(Long id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Equipo no encontrado"));
    }
}
