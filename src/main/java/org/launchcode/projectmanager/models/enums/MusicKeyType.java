package org.launchcode.projectmanager.models.enums;

public enum MusicKeyType {

    NONE (" "),
    A_FLAT_MAJOR ("A Flat Major"),
    A_MAJOR ("A Major"),
    A_MINOR ("A minor"),
    B_FLAT_MAJOR ("B Flat Major"),
    B_FLAT_MINOR ("B flat minor"),
    B_MAJOR ("B Major"),
    B_MINOR ("B minor"),
    C_MAJOR ("C Major"),
    C_MINOR ("C minor"),
    C_SHARP_MINOR ("C sharp minor"),
    D_MAJOR ("D Major"),
    D_MINOR ("D minor"),
    D_FLAT_MAJOR ("D Flat Major"),
    E_FLAT_MAJOR ("E Flat Major"),
    E_FLAT_MINOR ("E flat minor"),
    E_MAJOR ("E Major"),
    E_MINOR ("E minor"),
    F_MAJOR ("F Major"),
    F_MINOR ("F minor"),
    F_SHARP_MAJOR ("F Sharp Major"),
    F_SHARP_MINOR ("F sharp minor"),
    G_MAJOR ("G Major"),
    G_MINOR ("G minor"),
    G_SHARP_MINOR ("G sharp minor");

    private final String name;

    MusicKeyType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
