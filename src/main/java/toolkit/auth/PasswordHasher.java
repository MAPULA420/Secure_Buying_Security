package toolkit.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {

    private static final int COST = 12;

    public static String hash(char[] password) {
        return BCrypt.withDefaults().hashToString(COST, password);
    }

    public static boolean verify(char[] password, String hash) {
        return BCrypt.verifyer().verify(password, hash).verified;
    }
}
