package cl.duocuc.demo.msequipos.repository;

import cl.duocuc.demo.msequipos.entity.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrumentoRepository
        extends JpaRepository<Instrumento, Long> {

    Optional<Instrumento> findByNumeroSerie(String numeroSerie);

    boolean existsByNumeroSerie(String numeroSerie);
}
