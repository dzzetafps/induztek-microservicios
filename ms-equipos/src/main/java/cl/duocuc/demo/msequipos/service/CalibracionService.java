package cl.duocuc.demo.msequipos.service;

import cl.duocuc.demo.msequipos.dto.CalibracionResponse;
import cl.duocuc.demo.msequipos.dto.RegistrarCalibracionRequest;
import cl.duocuc.demo.msequipos.entity.Calibracion;
import cl.duocuc.demo.msequipos.entity.Instrumento;
import cl.duocuc.demo.msequipos.repository.CalibracionRepository;
import cl.duocuc.demo.msequipos.repository.InstrumentoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class CalibracionService {

    private final CalibracionRepository calibracionRepository;
    private final InstrumentoRepository instrumentoRepository;

    public CalibracionService(
            CalibracionRepository calibracionRepository,
            InstrumentoRepository instrumentoRepository) {
        this.calibracionRepository = calibracionRepository;
        this.instrumentoRepository = instrumentoRepository;
    }

    @Transactional
    public CalibracionResponse registrarCalibracion(
            RegistrarCalibracionRequest request) {

        if (request == null
                || request.instrumentoId() == null
                || request.instrumentoId() <= 0
                || request.fechaCalibracion() == null
                || request.fechaCalibracion().isBlank()
                || request.fechaVencimiento() == null
                || request.fechaVencimiento().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos obligatorios incompletos");
        }

        Instrumento instrumento = instrumentoRepository
                .findById(request.instrumentoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instrumento no encontrado"));

        LocalDate fechaCalibracion;
        LocalDate fechaVencimiento;

        try {
            fechaCalibracion = LocalDate.parse(
                    request.fechaCalibracion().trim());

            fechaVencimiento = LocalDate.parse(
                    request.fechaVencimiento().trim());

        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fecha invalida. Utilice AAAA-MM-DD");
        }

        if (fechaVencimiento.isBefore(fechaCalibracion)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de vencimiento no puede ser anterior a la calibracion");
        }

        Calibracion calibracion = new Calibracion();
        calibracion.setInstrumento(instrumento);
        calibracion.setFechaCalibracion(
                fechaCalibracion.toString());
        calibracion.setFechaVencimiento(
                fechaVencimiento.toString());

        Calibracion guardada = calibracionRepository.save(calibracion);

        return convertirRespuesta(guardada);
    }

    @Transactional(readOnly = true)
    public List<CalibracionResponse> listarCalibraciones(
            Long instrumentoId) {

        List<Calibracion> calibraciones;

        if (instrumentoId != null) {
            calibraciones = calibracionRepository
                    .findByInstrumento_Id(instrumentoId);
        } else {
            calibraciones = calibracionRepository.findAll();
        }

        return calibraciones.stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public CalibracionResponse obtenerCalibracion(Long id) {
        Calibracion calibracion = calibracionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Calibracion no encontrada"));

        return convertirRespuesta(calibracion);
    }

    private CalibracionResponse convertirRespuesta(
            Calibracion calibracion) {

        return new CalibracionResponse(
                calibracion.getId(),
                calibracion.getInstrumento().getId(),
                calibracion.getFechaCalibracion(),
                calibracion.getFechaVencimiento()
        );
    }
}
