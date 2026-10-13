package cl.respirarysoltar.clinico.repository;

import cl.respirarysoltar.clinico.domain.NotaClinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotaClinicaRepository extends JpaRepository<NotaClinica, UUID> {

    List<NotaClinica> findByFichaIdOrderByCreadoEnDesc(UUID fichaId);
}
