package org.launchcode.projectmanager.models.enums;

public enum TimeSignatureDenominator {

    NONE(" "),
    ONE("1"),
    TWO("2"),
    FOUR("4"),
    EIGHT("8"),
    SIXTEEN("16"),
    THIRTY_TWO("32"),
    SIXTY_FOUR("64"),
    ONE_HUNDRED_TWENTY_EIGHT("128"),
    TWO_HUNDRED_FIFTY_SIX("256");


    private final String name;

    TimeSignatureDenominator(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
