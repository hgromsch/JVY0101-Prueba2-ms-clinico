package cl.respirarysoltar.clinico.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CifradoServiceTest {

    private final CifradoService cifradoService = new CifradoService("");

    @Test
    void elContenidoCifradoNoEsTextoPlanoYSeDescifraCorrectamente() {
        String contenido = "El paciente reporta avances en el manejo de la ansiedad.";

        String cifrado = cifradoService.cifrar(contenido);

        assertNotEquals(contenido, cifrado);
        assertEquals(contenido, cifradoService.descifrar(cifrado));
    }

    @Test
    void dosCifradosDelMismoTextoDifierenPorElIvAleatorio() {
        String contenido = "Nota de sesión";

        assertNotEquals(cifradoService.cifrar(contenido), cifradoService.cifrar(contenido));
    }
}
