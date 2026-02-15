package ru.fkdev.donmarket.model;

public enum SortMode {
    BEST_RATIO("Выгодное по соотношению"),
    MOST_EXPENSIVE_COINS("Самые дорогие (по монетам)"),
    CHEAPEST_COINS("Самые дешёвые (по монетам)"),
    MOST_EXPENSIVE_TOKENS("Самые дорогие (по биржам)"),
    CHEAPEST_TOKENS("Самые дешёвые (по биржам)");

    private final String displayName;

    SortMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SortMode next() {
        SortMode[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
