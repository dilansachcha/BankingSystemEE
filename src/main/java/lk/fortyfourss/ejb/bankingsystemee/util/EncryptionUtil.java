package lk.fortyfourss.ejb.bankingsystemee.util;

import org.mindrot.jbcrypt.BCrypt;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {

    //BCrypt
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    //BCrypt AND legacy SHA-256
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (hashedPassword == null) return false;

        if (hashedPassword.startsWith("$2a$")) {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        }
        else if (hashedPassword.length() == 64) {
            return legacySha256Hash(plainTextPassword).equals(hashedPassword);
        }
        return false;
    }

    private static String legacySha256Hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}