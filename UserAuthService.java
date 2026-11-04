import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UserAuthService {
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH_BITS = 256;
    private static final int HASH_ITERATIONS = 120_000;

    private final ArrayList<UserAccount> accounts = new ArrayList<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public synchronized UserAccount register(String username, char[] password, UserRole role) {
        if (accounts.isEmpty()) {
            if (role != UserRole.ADMINISTRATOR) {
                throw new IllegalArgumentException(
                        "The first account must be an Administrator account.");
            }
        } else if (role == UserRole.ADMINISTRATOR) {
            throw new IllegalArgumentException(
                    "Administrator accounts can only be created by an Administrator.");
        }
        return addAccount(username, password, role);
    }

    public synchronized UserAccount createAccount(
            String username, char[] password, UserRole role, UserRole creatorRole) {
        if (creatorRole != UserRole.ADMINISTRATOR) {
            throw new IllegalArgumentException("Only an Administrator can create accounts.");
        }
        return addAccount(username, password, role);
    }

    public synchronized UserAccount authenticate(String username, char[] password) {
        if (username == null || password == null) {
            return null;
        }
        UserAccount account = findAccount(username);
        if (account == null) {
            hashPassword(password, new byte[SALT_LENGTH]);
            return null;
        }
        return account.hasPassword(hashPassword(password, account.getPasswordSalt()))
                ? account : null;
    }

    public synchronized int getAccountCount() {
        return accounts.size();
    }

    private UserAccount addAccount(String username, char[] password, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        if (role == null) {
            throw new IllegalArgumentException("User role is required.");
        }
        if (findAccount(username) != null) {
            throw new IllegalArgumentException("That username is already registered.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        UserAccount account = new UserAccount(
                username.trim(), role, salt, hashPassword(password, salt));
        accounts.add(account);
        return account;
    }

    private UserAccount findAccount(String username) {
        for (UserAccount account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username.trim())) {
                return account;
            }
        }
        return null;
    }

    private byte[] hashPassword(char[] password, byte[] salt) {
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        } finally {
            keySpec.clearPassword();
        }
    }
}
