public final class UserAccount {
    private final String username;
    private final UserRole role;
    private final byte[] passwordSalt;
    private final byte[] passwordHash;

    UserAccount(String username, UserRole role, byte[] passwordSalt, byte[] passwordHash) {
        this.username = username;
        this.role = role;
        this.passwordSalt = passwordSalt.clone();
        this.passwordHash = passwordHash.clone();
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    boolean hasPassword(byte[] candidateHash) {
        return java.security.MessageDigest.isEqual(passwordHash, candidateHash);
    }

    byte[] getPasswordSalt() {
        return passwordSalt.clone();
    }
}
