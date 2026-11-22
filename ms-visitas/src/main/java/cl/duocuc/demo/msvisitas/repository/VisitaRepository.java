package cl.duocuc.demo.msvisitas.repository;

import cl.duocuc.demo.msvisitas.entity.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    Optional<Visita> findByNumeroOt(String numeroOt);

    boolean existsByNumeroOt(String numeroOt);

    List<Visita> findByClienteIdOrderByFechaMillisDesc(String clienteId);

    List<Visita> findByClienteIdAndInstalacionIdOrderByFechaMillisDesc(
            String clienteId,
            String instalacionId
    );
}
