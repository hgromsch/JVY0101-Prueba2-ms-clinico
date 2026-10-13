package cl.respirarysoltar.clinico.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vista de una nota clínica con su contenido ya descifrado. Solo se expone a
 * roles autorizados (psicólogo tratante o admin); nunca se loguea.
 */
public record NotaResponse(
        UUID id,
        UUID psicologoId,
        String contenido,
        LocalDateTime creadoEn
) {
}
