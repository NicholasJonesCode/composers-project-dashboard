package org.launchcode.projectmanager.models.enums;

public enum Mode {

    NONE(""),
    IONIAN("Ionian"),
    DORIAN("Dorian"),
    PHRYGIAN("Phrygian"),
    LYDIAN("Lydian"),
    MIXOLYDIAN("Mixolydian"),
    AEOLIAN("Aeolian"),
    LOCRIAN("Locrian");


    private final String name;

    Mode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
