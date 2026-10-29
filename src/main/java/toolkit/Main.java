package toolkit;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;
import toolkit.audit.AuditLog;
import toolkit.auth.LockoutTracker;
import toolkit.auth.PasswordHasher;
import toolkit.auth.PasswordStrengthChecker;
import toolkit.auth.PasswordStrengthChecker.Strength;
import toolkit.auth.Permissions;
import toolkit.auth.Permissions.Action;
import toolkit.auth.Permissions.Role;
import toolkit.auth.UserStore;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final UserStore USERS = new UserStore(Path.of("data", "users.txt"));
    private static final LockoutTracker LOCKOUT = new LockoutTracker(Path.of("data", "lockouts.txt"));
    private static final AuditLog AUDIT = new AuditLog(Path.of("data", "audit.log"));

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "check" -> runCheck();
            case "register" -> runRegister();
            case "login" -> runLogin();
            case "audit" -> runAudit(args);
            default -> {
                System.out.println("Unknown command: " + args[0]);
                printUsage();
            }
        }
    }

    private static void runCheck() {
        char[] password = readPassword("Enter a password to check: ");
        printResult(PasswordStrengthChecker.check(new String(password)));
        Arrays.fill(password, ' ');
    }

    private static void runRegister() {
        System.out.print("Choose a username (3-20 letters, numbers or _): ");
        String username = SCANNER.nextLine().trim();
        if (!isValidUsername(username)) {
            System.out.println("Invalid username.");
            return;
        }

        try {
            if (USERS.exists(username)) {
                System.out.println("That username is already taken.");
                return;
            }

            System.out.print("Choose a role (buyer, seller or courier): ");
            Role role = Role.parse(SCANNER.nextLine());
            if (role == null) {
                System.out.println("Invalid role.");
                return;
            }

            char[] password = readPassword("Choose a password: ");
            PasswordStrengthChecker.Result result =
                    PasswordStrengthChecker.check(new String(password));

            if (result.strength() == Strength.WEAK) {
                System.out.println("Password rejected: too weak.");
                printResult(result);
                Arrays.fill(password, ' ');
                AUDIT.append("REGISTER_REJECTED user=" + username + " reason=weak_password");
                return;
            }

            USERS.add(username, PasswordHasher.hash(password), role);
            Arrays.fill(password, ' ');
            AUDIT.append("REGISTERED user=" + username + " role=" + role);
            System.out.println("Registered " + username + " as " + role
                    + ". Only a BCrypt hash was stored.");
        } catch (IOException e) {
            System.out.println("Could not access the data files: " + e.getMessage());
        }
    }

    private static void runLogin() {
        System.out.print("Username: ");
        String username = SCANNER.nextLine().trim();
        if (!isValidUsername(username)) {
            System.out.println("Invalid username or password.");
            return;
        }

        try {
            long wait = LOCKOUT.secondsRemaining(username);
            if (wait > 0) {
                AUDIT.append("LOGIN_BLOCKED user=" + username);
                System.out.println("Account is locked. Try again in " + wait + " seconds.");
                return;
            }

            char[] password = readPassword("Password: ");
            String hash = USERS.getHash(username);
            boolean ok = hash != null && PasswordHasher.verify(password, hash);
            Arrays.fill(password, ' ');

            if (!ok) {
                AUDIT.append("LOGIN_FAILED user=" + username);
                System.out.println("Invalid username or password.");
                if (hash != null && LOCKOUT.recordFailure(username)) {
                    AUDIT.append("ACCOUNT_LOCKED user=" + username);
                    System.out.println("Too many failed attempts. Account locked for "
                            + LockoutTracker.LOCK_SECONDS + " seconds.");
                }
                return;
            }

            LOCKOUT.reset(username);
            Role role = USERS.getRole(username);
            AUDIT.append("LOGIN_OK user=" + username + " role=" + role);
            System.out.println("Welcome, " + username + ". Your role is " + role + ".");
            runSession(username, role);
        } catch (IOException e) {
            System.out.println("Could not access the data files: " + e.getMessage());
        }
    }

    private static void runSession(String username, Role role) throws IOException {
        System.out.println("Try an action: " + Arrays.toString(Action.values()).toLowerCase()
                .replace('_', '-') + " (or quit)");
        while (true) {
            System.out.print(username + "> ");
            if (!SCANNER.hasNextLine()) {
                return;
            }
            String line = SCANNER.nextLine().trim();
            if (line.equalsIgnoreCase("quit")) {
                return;
            }

            Action action = Action.parse(line);
            if (action == null) {
                System.out.println("Unknown action.");
            } else if (Permissions.isAllowed(role, action)) {
                AUDIT.append("ACTION_ALLOWED user=" + username + " role=" + role + " action=" + action);
                System.out.println("Allowed. (Simulated: the real " + action + " happens in Secure_Buying.)");
            } else {
                AUDIT.append("ACTION_DENIED user=" + username + " role=" + role + " action=" + action);
                System.out.println("Denied: " + action + " is " + action.allowedRole()
                        + "-only, and you are a " + role + ".");
            }
        }
    }

    private static void runAudit(String[] args) {
        try {
            if (args.length >= 3 && args[1].equals("add")) {
                AUDIT.append(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                System.out.println("Logged.");
            } else if (args.length >= 2 && args[1].equals("verify")) {
                AuditLog.VerifyResult result = AUDIT.verify();
                if (result.valid()) {
                    System.out.println("Audit log OK: the chain is intact.");
                } else {
                    System.out.println("TAMPERING DETECTED at line " + result.brokenLine());
                }
            } else {
                System.out.println("Usage: audit add <event> | audit verify");
            }
        } catch (IOException e) {
            System.out.println("Could not access the audit log: " + e.getMessage());
        }
    }

    private static boolean isValidUsername(String username) {
        return username.matches("[A-Za-z0-9_]{3,20}");
    }

    private static char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword("%s", prompt);
            if (password != null) {
                return password;
            }
        }
        System.out.print(prompt);
        return SCANNER.nextLine().toCharArray();
    }

    private static void printResult(PasswordStrengthChecker.Result result) {
        System.out.println("Strength: " + result.strength()
                + " (score " + result.score() + "/" + PasswordStrengthChecker.MAX_SCORE + ")");
        if (result.feedback().isEmpty()) {
            System.out.println("No issues found.");
        } else {
            for (String tip : result.feedback()) {
                System.out.println("- " + tip);
            }
        }
    }

    private static void printUsage() {
        System.out.println("Secure Buying Security");
        System.out.println("Commands: check | register | login | audit");
    }
}