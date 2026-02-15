package ru.fkdev.donmarket.model;

public enum Category {
    ALL("Все"),
    PRIVILEGES("Привилегии"),
    TOKENS("Токены"),
    CASES("Кейсы"),
    UNBAN_UNMUTE("Разбан/Размут"),
    PASS("Пропуск");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category next() {
        Category[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
