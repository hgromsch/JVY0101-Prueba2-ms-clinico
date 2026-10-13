package cl.respirarysoltar.clinico.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos de entrada para registrar una nota clínica en una ficha (RF-09). */
public record NotaRequest(
        @NotBlank(message = "El contenido de la nota es obligatorio")
        @Size(max = 5000)
        String contenido,

        @NotNull(message = "El psicólogo que redacta es obligatorio")
        java.util.UUID psicologoId
) {
}
