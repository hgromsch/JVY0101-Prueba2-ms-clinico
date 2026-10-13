package cl.respirarysoltar.clinico.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Datos de entrada para abrir la ficha clínica de un paciente (RF-09). */
public record FichaRequest(
        @NotNull(message = "El paciente es obligatorio")
        UUID pacienteId,

        @Size(max = 2000)
        String antecedentes
) {
}
