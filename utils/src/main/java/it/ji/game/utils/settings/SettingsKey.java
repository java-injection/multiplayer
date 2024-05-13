package it.ji.game.utils.settings;

public enum SettingsKey {
    GAME_NAME("game.name"),
    MATRIX_ROWS("matrix.rows"),
    MATRIX_COLUMNS("matrix.columns");

    private String key;

    private SettingsKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static SettingsKey fromString(String key) {
        for (SettingsKey sk : SettingsKey.values()) {
            if (sk.getKey().equals(key)) {
                return sk;
            }
        }
        return null;
    }


}
