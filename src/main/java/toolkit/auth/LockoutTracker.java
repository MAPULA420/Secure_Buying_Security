package toolkit.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Temporary account lockout after repeated failed logins.
 * The lock expires on its own, so an attacker cannot lock someone out for good.
 * State is kept in a file so it survives between runs. Line format: username:failures:lockedUntilMillis
 */
public class LockoutTracker {

    public static final int MAX_FAILURES = 3;
    public static final long LOCK_SECONDS = 60;

    private final Path file;

    public LockoutTracker(Path file) {
        this.file = file;
    }

    /** Seconds left on the lock, or 0 if the account is not locked. */
    public long secondsRemaining(String username) throws IOException {
        long[] entry = load().get(username);
        if (entry == null) {
            return 0;
        }
        long remainingMillis = entry[1] - System.currentTimeMillis();
        return remainingMillis > 0 ? (remainingMillis + 999) / 1000 : 0;
    }

    /** Records a failed login. Returns true if this failure locked the account. */
    public boolean recordFailure(String username) throws IOException {
        Map<String, long[]> state = load();
        long now = System.currentTimeMillis();
        long[] entry = state.getOrDefault(username, new long[] {0, 0});

        if (entry[1] != 0 && entry[1] <= now) {   // an old lock has expired: start fresh
            entry[0] = 0;
            entry[1] = 0;
        }

        entry[0]++;
        boolean locked = false;
        if (entry[0] >= MAX_FAILURES) {
            entry[0] = 0;
            entry[1] = now + LOCK_SECONDS * 1000;
            locked = true;
        }

        state.put(username, entry);
        save(state);
        return locked;
    }

    /** Clears the failure count after a successful login. */
    public void reset(String username) throws IOException {
        Map<String, long[]> state = load();
        if (state.remove(username) != null) {
            save(state);
        }
    }

    private Map<String, long[]> load() throws IOException {
        Map<String, long[]> state = new HashMap<>();
        if (!Files.exists(file)) {
            return state;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split(":");
            if (parts.length == 3) {
                try {
                    state.put(parts[0], new long[] {Long.parseLong(parts[1]), Long.parseLong(parts[2])});
                } catch (NumberFormatException ignored) {
                    // skip a damaged line
                }
            }
        }
        return state;
    }

    private void save(Map<String, long[]> state) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, long[]> e : state.entrySet()) {
            out.append(e.getKey()).append(':').append(e.getValue()[0])
                    .append(':').append(e.getValue()[1]).append(System.lineSeparator());
        }
        Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
    }
}