package cl.duocuc.demo.msequipos.repository;

import cl.duocuc.demo.msequipos.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findByNumeroSerie(String numeroSerie);

    boolean existsByNumeroSerie(String numeroSerie);

    List<Equipo> findByClienteId(String clienteId);

    List<Equipo> findByClienteIdAndInstalacionId(
            String clienteId,
            String instalacionId
    );
}
