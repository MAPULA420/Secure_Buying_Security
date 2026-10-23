package toolkit;

import java.util.Scanner;
import toolkit.auth.PasswordStrengthChecker;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "check" -> runCheck();
            default -> {
                System.out.println("Unknown command: " + args[0]);
                printUsage();
            }
        }
    }

    private static void runCheck() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a password to check: ");
        String password = scanner.nextLine();

        PasswordStrengthChecker.Result result = PasswordStrengthChecker.check(password);

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
        System.out.println("Commands: check | register | log add | log verify");
    }
}
