package cl.duocuc.demo.msequipos.service;

import cl.duocuc.demo.msequipos.dto.RegistrarInstrumentoRequest;
import cl.duocuc.demo.msequipos.entity.Instrumento;
import cl.duocuc.demo.msequipos.repository.InstrumentoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class InstrumentoService {

    private final InstrumentoRepository instrumentoRepository;

    public InstrumentoService(
            InstrumentoRepository instrumentoRepository) {
        this.instrumentoRepository = instrumentoRepository;
    }

    @Transactional
    public Instrumento registrarInstrumento(
            RegistrarInstrumentoRequest request) {

        if (request == null
                || request.nombre() == null
                || request.nombre().isBlank()
                || request.tipoInstrumento() == null
                || request.tipoInstrumento().isBlank()
                || request.marca() == null
                || request.marca().isBlank()
                || request.numeroSerie() == null
                || request.numeroSerie().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos obligatorios incompletos");
        }

        String numeroSerie = request.numeroSerie().trim();

        if (instrumentoRepository.existsByNumeroSerie(numeroSerie)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un instrumento con este numero de serie");
        }

        String fecha = request.fechaVencimientoCalibracion();

        if (fecha != null && !fecha.isBlank()) {
            fecha = fecha.trim();

            try {
                LocalDate.parse(fecha);
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Fecha invalida. Utilice AAAA-MM-DD");
            }
        } else {
            fecha = null;
        }

        Instrumento instrumento = new Instrumento();

        instrumento.setNombre(request.nombre().trim());
        instrumento.setTipoInstrumento(
                request.tipoInstrumento().trim());
        instrumento.setMarca(request.marca().trim());
        instrumento.setModelo(request.modelo());
        instrumento.setNumeroSerie(numeroSerie);
        instrumento.setCodigoInterno(request.codigoInterno());
        instrumento.setFechaVencimientoCalibracion(fecha);
        instrumento.setObservaciones(request.observaciones());

        return instrumentoRepository.save(instrumento);
    }

    @Transactional(readOnly = true)
    public List<Instrumento> listarInstrumentos() {
        return instrumentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Instrumento obtenerInstrumento(Long id) {
        return instrumentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instrumento no encontrado"));
    }
}
