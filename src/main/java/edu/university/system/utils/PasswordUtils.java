package edu.university.system.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Utilidad de contrasenas. Genera y compara hashes SHA-256 compatibles con los
 * usuarios iniciales y limpia arreglos char[] luego de usarlos.
 */
public final class PasswordUtils {

    public static final String SHA_256 = "SHA-256";

    private PasswordUtils() {
    }

    public static String hashSha256(char[] password) {
        if (password == null) {
            throw new IllegalArgumentException("La contrasena es requerida.");
        }

        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return toHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se encontro el algoritmo SHA-256.", exception);
        } finally {
            Arrays.fill(bytes, (byte) 0);
            if (byteBuffer.hasArray()) {
                Arrays.fill(byteBuffer.array(), (byte) 0);
            }
        }
    }

    public static boolean matches(char[] password, String expectedHash) {
        if (expectedHash == null || expectedHash.isBlank()) {
            return false;
        }
        String actualHash = hashSha256(password);
        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.trim().getBytes(StandardCharsets.UTF_8)
        );
    }

    public static void clear(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
