package cl.respirarysoltar.clinico.exception;

/**
 * Se lanza cuando el rol invocante no tiene permiso sobre el recurso clínico
 * (HTTP 403 Forbidden). En producción el rol sale del JWT de Cognito.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
