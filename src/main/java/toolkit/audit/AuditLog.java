package toolkit.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

public class AuditLog {

    public record VerifyResult(boolean valid, int brokenLine) {}

    private static final String GENESIS = "0".repeat(64);
    private final Path file;

    public AuditLog(Path file) {
        this.file = file;
    }

    // Line format: time|event|previousHash|hash
    public void append(String event) throws IOException {
        String clean = event.replace("|", "/").replace("\n", " ").replace("\r", " ");
        String previous = lastHash();
        String time = Instant.now().toString();
        String hash = hash(previous, time, clean);

        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.writeString(file,
                time + "|" + clean + "|" + previous + "|" + hash + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public VerifyResult verify() throws IOException {
        if (!Files.exists(file)) {
            return new VerifyResult(true, 0);
        }
        List<String> lines = Files.readAllLines(file);
        String expectedPrevious = GENESIS;

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\|", -1);
            if (parts.length != 4
                    || !parts[2].equals(expectedPrevious)
                    || !hash(parts[2], parts[0], parts[1]).equals(parts[3])) {
                return new VerifyResult(false, i + 1);
            }
            expectedPrevious = parts[3];
        }
        return new VerifyResult(true, 0);
    }

    private String lastHash() throws IOException {
        if (!Files.exists(file)) {
            return GENESIS;
        }
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) {
            return GENESIS;
        }
        String[] parts = lines.get(lines.size() - 1).split("\\|", -1);
        return parts.length == 4 ? parts[3] : GENESIS;
    }

    private static String hash(String previous, String time, String event) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha.digest((previous + "|" + time + "|" + event)
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}