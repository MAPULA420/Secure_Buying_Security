package toolkit.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class UserStore {

    private final Path file;

    public UserStore(Path file) {
        this.file = file;
    }

    public boolean exists(String username) throws IOException {
        return load().containsKey(username);
    }

    public String getHash(String username) throws IOException {
        return load().get(username);
    }

    public void add(String username, String passwordHash) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.writeString(file, username + ":" + passwordHash + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private Map<String, String> load() throws IOException {
        Map<String, String> users = new HashMap<>();
        if (!Files.exists(file)) {
            return users;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            int i = line.indexOf(':');
            if (i > 0) {
                users.put(line.substring(0, i), line.substring(i + 1));
            }
        }
        return users;
    }
}
