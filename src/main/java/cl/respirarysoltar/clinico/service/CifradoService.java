package cl.respirarysoltar.clinico.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado a nivel de aplicación del contenido de las notas clínicas (RF-09).
 * Usa AES-GCM con un IV aleatorio por nota. En AWS la clave la custodia KMS y se
 * inyecta como variable de entorno (CLINICO_CRYPTO_KEY en Base64 de 32 bytes);
 * localmente se usa una clave de desarrollo solo para demostración.
 */
@Service
public class CifradoService {

    private static final Logger log = LoggerFactory.getLogger(CifradoService.class);
    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int LARGO_IV = 12;
    private static final int LARGO_TAG_BITS = 128;
    /** Clave de desarrollo de 32 bytes (AES-256). Nunca usar en producción. */
    private static final String CLAVE_DEV = "0123456789abcdef0123456789abcdef";

    private final SecretKeySpec clave;

    public CifradoService(@Value("${CLINICO_CRYPTO_KEY:}") String claveBase64) {
        byte[] bytes = (claveBase64 == null || claveBase64.isBlank())
                ? CLAVE_DEV.getBytes(StandardCharsets.UTF_8)
                : Base64.getDecoder().decode(claveBase64);
        this.clave = new SecretKeySpec(bytes, "AES");
        if (claveBase64 == null || claveBase64.isBlank()) {
            log.warn("Usando clave de cifrado de DESARROLLO; configure CLINICO_CRYPTO_KEY en el entorno");
        }
    }

    /** Cifra texto plano y devuelve Base64(iv + textoCifrado). */
    public String cifrar(String textoPlano) {
        try {
            byte[] iv = new byte[LARGO_IV];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(LARGO_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            byte[] combinado = ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array();
            return Base64.getEncoder().encodeToString(combinado);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar la nota clínica", e);
        }
    }

    /** Descifra el contenido persistido. Solo invocar tras validar la autorización. */
    public String descifrar(String textoCifradoBase64) {
        try {
            byte[] combinado = Base64.getDecoder().decode(textoCifradoBase64);
            ByteBuffer buffer = ByteBuffer.wrap(combinado);
            byte[] iv = new byte[LARGO_IV];
            buffer.get(iv);
            byte[] cifrado = new byte[buffer.remaining()];
            buffer.get(cifrado);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(LARGO_TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo descifrar la nota clínica", e);
        }
    }
}
