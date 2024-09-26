package eu.minted.eos.model;

public enum Role {
    ADMIN(0),
    USER(1);

    private final int value;

    Role(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Role getRole(int value) {
        switch (value) {
            case 0:
                return ADMIN;
            case 1:
                return USER;
            default:
                throw new IllegalArgumentException("Unknown Role value: " + value);
        }
    }
}
