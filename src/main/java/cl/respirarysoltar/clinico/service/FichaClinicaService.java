package cl.respirarysoltar.clinico.service;

import cl.respirarysoltar.clinico.api.dto.ActualizarFichaRequest;
import cl.respirarysoltar.clinico.api.dto.FichaRequest;
import cl.respirarysoltar.clinico.api.dto.NotaRequest;
import cl.respirarysoltar.clinico.api.dto.NotaResponse;
import cl.respirarysoltar.clinico.domain.FichaClinica;
import cl.respirarysoltar.clinico.domain.NotaClinica;
import cl.respirarysoltar.clinico.exception.AccesoDenegadoException;
import cl.respirarysoltar.clinico.exception.RecursoNoEncontradoException;
import cl.respirarysoltar.clinico.exception.ReglaNegocioException;
import cl.respirarysoltar.clinico.repository.FichaClinicaRepository;
import cl.respirarysoltar.clinico.repository.NotaClinicaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio de las fichas clínicas (RF-09). Zona crítica:
 * - solo roles PSICOLOGO o ADMIN pueden leer el contenido de las notas;
 * - el contenido se cifra antes de persistir y se descifra solo en memoria;
 * - cada acceso queda registrado en el log de auditoría.
 * En producción el rol y la relación paciente-psicólogo se validan contra el JWT
 * de Cognito; localmente se recibe el rol en el header X-Usuario-Rol.
 */
@Service
@Transactional
public class FichaClinicaService {

    private static final Logger logAuditoria = LoggerFactory.getLogger("cl.respirarysoltar.clinico.auditoria");

    private final FichaClinicaRepository fichaRepository;
    private final NotaClinicaRepository notaRepository;
    private final CifradoService cifradoService;

    public FichaClinicaService(FichaClinicaRepository fichaRepository,
                               NotaClinicaRepository notaRepository,
                               CifradoService cifradoService) {
        this.fichaRepository = fichaRepository;
        this.notaRepository = notaRepository;
        this.cifradoService = cifradoService;
    }

    public FichaClinica crear(FichaRequest request) {
        if (fichaRepository.existsByPacienteId(request.pacienteId())) {
            throw new ReglaNegocioException("El paciente ya tiene una ficha clínica abierta");
        }
        FichaClinica ficha = new FichaClinica(request.pacienteId(), request.antecedentes());
        FichaClinica guardada = fichaRepository.save(ficha);
        logAuditoria.info("FICHA_CREADA fichaId={} pacienteId={}", guardada.getId(), guardada.getPacienteId());
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<FichaClinica> listar() {
        return fichaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FichaClinica obtener(UUID id) {
        return fichaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ficha clínica no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public FichaClinica obtenerPorPaciente(UUID pacienteId) {
        return fichaRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El paciente no tiene ficha clínica: " + pacienteId));
    }

    public FichaClinica actualizarAntecedentes(UUID id, ActualizarFichaRequest request) {
        FichaClinica ficha = obtener(id);
        ficha.setAntecedentes(request.antecedentes());
        logAuditoria.info("FICHA_ACTUALIZADA fichaId={}", id);
        return fichaRepository.save(ficha);
    }

    public void eliminar(UUID id) {
        FichaClinica ficha = obtener(id);
        fichaRepository.delete(ficha);
        logAuditoria.info("FICHA_ELIMINADA fichaId={}", id);
    }

    /** Registra una nota de sesión cifrando su contenido antes de persistir. */
    public NotaClinica agregarNota(UUID fichaId, NotaRequest request) {
        FichaClinica ficha = obtener(fichaId);
        NotaClinica nota = new NotaClinica(cifradoService.cifrar(request.contenido()), request.psicologoId());
        ficha.agregarNota(nota);
        NotaClinica guardada = notaRepository.save(nota);
        logAuditoria.info("NOTA_REGISTRADA fichaId={} notaId={} psicologoId={}", fichaId, guardada.getId(), request.psicologoId());
        return guardada;
    }

    /** Devuelve las notas descifradas solo si el rol invocante está autorizado. */
    @Transactional(readOnly = true)
    public List<NotaResponse> listarNotas(UUID fichaId, String rolInvocante) {
        obtener(fichaId);
        validarAcceso(fichaId, rolInvocante);
        return notaRepository.findByFichaIdOrderByCreadoEnDesc(fichaId).stream()
                .map(nota -> new NotaResponse(nota.getId(), nota.getPsicologoId(),
                        cifradoService.descifrar(nota.getContenidoCifrado()), nota.getCreadoEn()))
                .toList();
    }

    private void validarAcceso(UUID fichaId, String rolInvocante) {
        boolean autorizado = "PSICOLOGO".equals(rolInvocante) || "ADMIN".equals(rolInvocante);
        logAuditoria.info("ACCESO_NOTAS fichaId={} rol={} resultado={}", fichaId, rolInvocante,
                autorizado ? "PERMITIDO" : "DENEGADO");
        if (!autorizado) {
            throw new AccesoDenegadoException("Solo un psicólogo o administrador puede leer las notas clínicas");
        }
    }
}
