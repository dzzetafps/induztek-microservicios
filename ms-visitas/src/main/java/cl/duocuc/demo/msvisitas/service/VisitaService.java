package cl.duocuc.demo.msvisitas.service;

import cl.duocuc.demo.msvisitas.dto.RegistrarVisitaRequest;
import cl.duocuc.demo.msvisitas.entity.Visita;
import cl.duocuc.demo.msvisitas.entity.VisitaTecnico;
import cl.duocuc.demo.msvisitas.repository.VisitaRepository;
import cl.duocuc.demo.msvisitas.repository.VisitaTecnicoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final VisitaTecnicoRepository tecnicoRepository;

    public VisitaService(
            VisitaRepository visitaRepository,
            VisitaTecnicoRepository tecnicoRepository) {
        this.visitaRepository = visitaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public Visita registrarVisita(RegistrarVisitaRequest request) {

        if (request == null
                || request.clienteId() == null
                || request.clienteId().isBlank()
                || request.instalacionId() == null
                || request.instalacionId().isBlank()
                || request.fechaMillis() == null
                || request.numeroOt() == null
                || request.numeroOt().isBlank()
                || request.tipoVisita() == null
                || request.ingenieroCargoId() == null
                || request.ingenieroCargoId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos obligatorios incompletos");
        }

        if (!request.tipoVisita().equals("SOLO_PRUEBAS")
                && !request.tipoVisita().equals("PRUEBAS_Y_MANT")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de visita no válido");
        }

        String numeroOt = request.numeroOt().trim();

        if (visitaRepository.existsByNumeroOt(numeroOt)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una visita con este número de OT");
        }

        Set<String> tecnicos = new LinkedHashSet<>();

        if (request.tecnicoIds() != null) {
            for (String tecnicoId : request.tecnicoIds()) {

                if (tecnicoId == null || tecnicoId.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Técnico no válido");
                }

                if (!tecnicos.add(tecnicoId.trim())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Técnico duplicado");
                }
            }
        }

        Visita visita = new Visita();
        visita.setClienteId(request.clienteId());
        visita.setInstalacionId(request.instalacionId());
        visita.setFechaMillis(request.fechaMillis());
        visita.setNumeroOt(numeroOt);
        visita.setTipoVisita(request.tipoVisita());
        visita.setIngenieroCargoId(request.ingenieroCargoId());
        visita.setObservaciones(request.observaciones());

        Visita guardada = visitaRepository.save(visita);

        List<VisitaTecnico> asociaciones = new ArrayList<>();

        for (String tecnicoId : tecnicos) {
            asociaciones.add(new VisitaTecnico(guardada, tecnicoId));
        }

        tecnicoRepository.saveAll(asociaciones);

        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Visita> listarVisitas() {
        return visitaRepository.findAll(
                Sort.by(Sort.Direction.DESC, "fechaMillis")
        );
    }

    @Transactional(readOnly = true)
    public Visita obtenerVisita(Long id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Visita no encontrada"
                ));
    }
}
