package cl.duocuc.demo.msvisitas.repository;

import cl.duocuc.demo.msvisitas.entity.VisitaTecnico;
import cl.duocuc.demo.msvisitas.entity.VisitaTecnicoId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisitaTecnicoRepository
        extends JpaRepository<VisitaTecnico, VisitaTecnicoId> {

    List<VisitaTecnico> findByVisita_Id(Long visitaId);
}
