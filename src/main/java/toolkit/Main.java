package toolkit;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;
import toolkit.auth.PasswordHasher;
import toolkit.auth.PasswordStrengthChecker;
import toolkit.auth.PasswordStrengthChecker.Strength;
import toolkit.auth.UserStore;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final UserStore USERS = new UserStore(Path.of("data", "users.txt"));

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "check" -> runCheck();
            case "register" -> runRegister();
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
        if (!username.matches("[A-Za-z0-9_]{3,20}")) {
            System.out.println("Invalid username.");
            return;
        }

        try {
            if (USERS.exists(username)) {
                System.out.println("That username is already taken.");
                return;
            }

            char[] password = readPassword("Choose a password: ");
            PasswordStrengthChecker.Result result =
                    PasswordStrengthChecker.check(new String(password));

            if (result.strength() == Strength.WEAK) {
                System.out.println("Password rejected: too weak.");
                printResult(result);
                Arrays.fill(password, ' ');
                return;
            }

            USERS.add(username, PasswordHasher.hash(password));
            Arrays.fill(password, ' ');
            System.out.println("Registered " + username + ". Only a BCrypt hash was stored.");
        } catch (IOException e) {
            System.out.println("Could not access the user file: " + e.getMessage());
        }
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
        System.out.println("Commands: check | register");
    }
}
