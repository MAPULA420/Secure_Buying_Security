package toolkit.auth;

/**
 * Role-based access control: each action in the marketplace belongs to one role.
 */
public final class Permissions {

    private Permissions() {}

    public enum Role {
        BUYER, SELLER, COURIER;

        public static Role parse(String text) {
            try {
                return valueOf(text.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum Action {
        UPLOAD_PHOTO(Role.SELLER),
        APPROVE_PHOTO(Role.BUYER),
        PAY(Role.BUYER),
        SHIP(Role.SELLER),
        CONFIRM_DELIVERY(Role.COURIER),
        REPORT_DAMAGE(Role.BUYER),
        CONFIRM_RETURN(Role.SELLER);

        private final Role allowedRole;

        Action(Role allowedRole) {
            this.allowedRole = allowedRole;
        }

        public Role allowedRole() {
            return allowedRole;
        }

        public static Action parse(String text) {
            try {
                return valueOf(text.trim().toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public static boolean isAllowed(Role role, Action action) {
        return action.allowedRole() == role;
    }
}