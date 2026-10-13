package cl.respirarysoltar.clinico.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Nota clínica de una sesión (lado "muchos" de ManyToOne con FichaClinica).
 * El contenido se cifra a nivel de aplicación antes de persistir (AES-GCM; en AWS
 * la clave la custodia KMS) y solo se descifra en memoria para el psicólogo tratante.
 * Nunca se expone en logs.
 */
@Entity
@Table(name = "notas_clinicas")
public class NotaClinica {

    @Id
    @GeneratedValue
    private UUID id;

    /** Contenido cifrado en Base64 (iv + texto cifrado). Jamás texto plano. */
    @Column(nullable = false, columnDefinition = "text")
    private String contenidoCifrado;

    /** Id del psicólogo que redactó la nota (su perfil vive en ms-identidad). */
    @Column(nullable = false)
    private UUID psicologoId;

    @Column(nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private FichaClinica ficha;

    protected NotaClinica() {
        // Constructor requerido por JPA
    }

    public NotaClinica(String contenidoCifrado, UUID psicologoId) {
        this.contenidoCifrado = contenidoCifrado;
        this.psicologoId = psicologoId;
    }

    public UUID getId() {
        return id;
    }

    public String getContenidoCifrado() {
        return contenidoCifrado;
    }

    public UUID getPsicologoId() {
        return psicologoId;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public FichaClinica getFicha() {
        return ficha;
    }

    public void setFicha(FichaClinica ficha) {
        this.ficha = ficha;
    }
}
