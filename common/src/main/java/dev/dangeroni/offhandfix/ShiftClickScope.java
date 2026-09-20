package dev.dangeroni.offhandfix;

public enum ShiftClickScope {
    PLAYER_INVENTORY_ONLY,
    ALL_CONTAINERS,
    DISABLED;

    public static ShiftClickScope parse(String value) {
        try {
            return valueOf(value == null ? "" : value.trim());
        } catch (IllegalArgumentException exception) {
            return PLAYER_INVENTORY_ONLY;
        }
    }
}
