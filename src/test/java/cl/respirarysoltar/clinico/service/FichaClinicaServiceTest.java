package cl.respirarysoltar.clinico.service;

import cl.respirarysoltar.clinico.api.dto.FichaRequest;
import cl.respirarysoltar.clinico.api.dto.NotaRequest;
import cl.respirarysoltar.clinico.domain.FichaClinica;
import cl.respirarysoltar.clinico.domain.NotaClinica;
import cl.respirarysoltar.clinico.exception.AccesoDenegadoException;
import cl.respirarysoltar.clinico.exception.ReglaNegocioException;
import cl.respirarysoltar.clinico.repository.FichaClinicaRepository;
import cl.respirarysoltar.clinico.repository.NotaClinicaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FichaClinicaServiceTest {

    @Mock
    private FichaClinicaRepository fichaRepository;

    @Mock
    private NotaClinicaRepository notaRepository;

    @Mock
    private CifradoService cifradoService;

    @InjectMocks
    private FichaClinicaService service;

    @Test
    void crearFichaParaPacienteQueYaTieneLanzaConflicto() {
        UUID pacienteId = UUID.randomUUID();
        when(fichaRepository.existsByPacienteId(pacienteId)).thenReturn(true);

        assertThrows(ReglaNegocioException.class,
                () -> service.crear(new FichaRequest(pacienteId, "antecedentes")));
    }

    @Test
    void agregarNotaPersisteElContenidoCifradoNoElPlano() {
        UUID fichaId = UUID.randomUUID();
        FichaClinica ficha = new FichaClinica(UUID.randomUUID(), "antecedentes");
        when(fichaRepository.findById(fichaId)).thenReturn(Optional.of(ficha));
        when(cifradoService.cifrar("contenido sensible")).thenReturn("BASE64_CIFRADO");
        when(notaRepository.save(any(NotaClinica.class))).thenAnswer(inv -> inv.getArgument(0));

        NotaClinica nota = service.agregarNota(fichaId, new NotaRequest("contenido sensible", UUID.randomUUID()));

        org.junit.jupiter.api.Assertions.assertEquals("BASE64_CIFRADO", nota.getContenidoCifrado());
    }

    @Test
    void listarNotasConRolPacienteEsDenegado() {
        UUID fichaId = UUID.randomUUID();
        FichaClinica ficha = new FichaClinica(UUID.randomUUID(), "antecedentes");
        when(fichaRepository.findById(fichaId)).thenReturn(Optional.of(ficha));

        assertThrows(AccesoDenegadoException.class, () -> service.listarNotas(fichaId, "PACIENTE"));
    }
}
