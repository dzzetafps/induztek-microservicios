package cl.duocuc.demo.msequipos.repository;

import cl.duocuc.demo.msequipos.entity.Calibracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalibracionRepository
        extends JpaRepository<Calibracion, Long> {

    List<Calibracion> findByInstrumento_Id(Long instrumentoId);

    boolean existsByInstrumento_Id(Long instrumentoId);
}
