package cl.respirarysoltar.clinico.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ficha clínica de un paciente (RF-09). Zona crítica de seguridad: cada acceso
 * queda auditado en logs y las notas asociadas se cifran a nivel de aplicación.
 * Existe a lo más una ficha por paciente.
 */
@Entity
@Table(name = "fichas_clinicas")
public class FichaClinica {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID pacienteId;

    @Column(length = 2000)
    private String antecedentes;

    @Column(nullable = false)
    private LocalDate fechaApertura = LocalDate.now();

    /** Relación OneToMany: notas de sesión de la ficha (contenido cifrado). */
    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotaClinica> notas = new ArrayList<>();

    protected FichaClinica() {
        // Constructor requerido por JPA
    }

    public FichaClinica(UUID pacienteId, String antecedentes) {
        this.pacienteId = pacienteId;
        this.antecedentes = antecedentes;
    }

    public void agregarNota(NotaClinica nota) {
        nota.setFicha(this);
        notas.add(nota);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public String getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public List<NotaClinica> getNotas() {
        return notas;
    }
}
