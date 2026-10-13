package cl.respirarysoltar.clinico.api;

import cl.respirarysoltar.clinico.api.dto.ActualizarFichaRequest;
import cl.respirarysoltar.clinico.api.dto.FichaRequest;
import cl.respirarysoltar.clinico.api.dto.NotaRequest;
import cl.respirarysoltar.clinico.api.dto.NotaResponse;
import cl.respirarysoltar.clinico.domain.FichaClinica;
import cl.respirarysoltar.clinico.domain.NotaClinica;
import cl.respirarysoltar.clinico.service.FichaClinicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * API REST de fichas clínicas (RF-09). Zona crítica de seguridad: la lectura de las
 * notas exige el header X-Usuario-Rol con valor PSICOLOGO o ADMIN (en AWS este dato
 * proviene del JWT de Cognito) y cada acceso queda auditado en el log del servicio.
 */
@RestController
@RequestMapping("/api/fichas")
public class FichaClinicaController {

    private final FichaClinicaService service;

    public FichaClinicaController(FichaClinicaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FichaClinica> crear(@Valid @RequestBody FichaRequest request) {
        FichaClinica creada = service.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping
    public List<FichaClinica> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public FichaClinica obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    public FichaClinica obtenerPorPaciente(@PathVariable UUID pacienteId) {
        return service.obtenerPorPaciente(pacienteId);
    }

    @PutMapping("/{id}")
    public FichaClinica actualizarAntecedentes(@PathVariable UUID id,
                                               @Valid @RequestBody ActualizarFichaRequest request) {
        return service.actualizarAntecedentes(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notas")
    public ResponseEntity<Void> agregarNota(@PathVariable UUID id, @Valid @RequestBody NotaRequest request) {
        NotaClinica nota = service.agregarNota(id, request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/fichas/{id}/notas").buildAndExpand(id).toUri();
        return ResponseEntity.created(ubicacion).build();
    }

    /** Solo roles PSICOLOGO o ADMIN; el contenido se devuelve descifrado en memoria. */
    @GetMapping("/{id}/notas")
    public List<NotaResponse> listarNotas(@PathVariable UUID id,
                                          @RequestHeader(name = "X-Usuario-Rol", required = false) String rol) {
        return service.listarNotas(id, rol);
    }
}
