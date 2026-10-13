package cl.respirarysoltar.clinico.api.dto;

import jakarta.validation.constraints.Size;

/** Datos de entrada para actualizar los antecedentes de una ficha clínica. */
public record ActualizarFichaRequest(
        @Size(max = 2000)
        String antecedentes
) {
}
