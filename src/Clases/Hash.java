package Clases;

import org.mindrot.jbcrypt.BCrypt;

public class Hash {

    /* @param password */
    public static String hashearContrasena(String password) {

        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    /**
     *
     * @param password
     * @param hashed
     */
    public static boolean verificarContrasena(String password, String hashed) {

        return BCrypt.checkpw(password, hashed);
    }
}
