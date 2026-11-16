package cafegest.dominio;

/** Infracción de una regla de negocio; el mensaje está pensado para mostrarse al operador. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
