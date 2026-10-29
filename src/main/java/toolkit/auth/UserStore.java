package toolkit.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import toolkit.auth.Permissions.Role;

/**
 * Stores users as username:bcryptHash:ROLE, one per line.
 * Older lines without a role (username:bcryptHash) are read as BUYER.
 */
public class UserStore {

    private final Path file;

    public UserStore(Path file) {
        this.file = file;
    }

    public boolean exists(String username) throws IOException {
        return load().containsKey(username);
    }

    public String getHash(String username) throws IOException {
        String[] record = load().get(username);
        return record == null ? null : record[0];
    }

    public Role getRole(String username) throws IOException {
        String[] record = load().get(username);
        if (record == null) {
            return null;
        }
        Role role = Role.parse(record[1]);
        return role == null ? Role.BUYER : role;
    }

    public void add(String username, String passwordHash, Role role) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.writeString(file, username + ":" + passwordHash + ":" + role + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private Map<String, String[]> load() throws IOException {
        Map<String, String[]> users = new HashMap<>();
        if (!Files.exists(file)) {
            return users;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            int i = line.indexOf(':');
            if (i <= 0) {
                continue;
            }
            String username = line.substring(0, i);
            String rest = line.substring(i + 1);
            int j = rest.lastIndexOf(':');
            if (j >= 0) {
                users.put(username, new String[] {rest.substring(0, j), rest.substring(j + 1)});
            } else {
                users.put(username, new String[] {rest, "BUYER"});
            }
        }
        return users;
    }
}