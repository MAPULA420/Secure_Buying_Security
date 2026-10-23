package toolkit.auth;

import java.util.ArrayList;
import java.util.List;

public class PasswordStrengthChecker {

    public enum Strength { WEAK, MEDIUM, STRONG }

    public record Result(Strength strength, int score, List<String> feedback) {}

    public static final int MAX_SCORE = 6;

    private static final List<String> COMMON_PASSWORDS = List.of(
            "password", "123456", "123456789", "12345678", "qwerty",
            "abc123", "letmein", "admin", "welcome", "iloveyou",
            "monkey", "dragon", "football", "111111", "password1");

    public static Result check(String password) {
        List<String> feedback = new ArrayList<>();
        int score = 0;

        // Length
        if (password.length() >= 8) {
            score++;
            if (password.length() >= 12) {
                score++;
            } else {
                feedback.add("Use 12 or more characters for a stronger password");
            }
        } else {
            feedback.add("Too short (use at least 8 characters)");
        }

        // Character variety
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSymbol = true;
        }

        if (hasUpper) score++; else feedback.add("Add an uppercase letter");
        if (hasLower) score++; else feedback.add("Add a lowercase letter");
        if (hasDigit) score++; else feedback.add("Add a number");
        if (hasSymbol) score++; else feedback.add("Add a symbol");

        // Common passwords
        String lower = password.toLowerCase();
        boolean isCommon = COMMON_PASSWORDS.contains(lower);
        if (isCommon) {
            feedback.add("This is a very common password");
        } else {
            for (String common : COMMON_PASSWORDS) {
                if (common.length() >= 5 && lower.contains(common)) {
                    feedback.add("Contains a common password: " + common);
                    score -= 2;
                    break;
                }
            }
        }
        score = Math.max(score, 0);

        Strength strength;
        if (isCommon) strength = Strength.WEAK;
        else if (score >= 5) strength = Strength.STRONG;
        else if (score >= 3) strength = Strength.MEDIUM;
        else strength = Strength.WEAK;

        return new Result(strength, score, feedback);
    }
}
