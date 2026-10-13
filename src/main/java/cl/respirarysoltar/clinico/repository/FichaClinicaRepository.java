package cl.respirarysoltar.clinico.repository;

import cl.respirarysoltar.clinico.domain.FichaClinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FichaClinicaRepository extends JpaRepository<FichaClinica, UUID> {

    Optional<FichaClinica> findByPacienteId(UUID pacienteId);

    boolean existsByPacienteId(UUID pacienteId);
}
