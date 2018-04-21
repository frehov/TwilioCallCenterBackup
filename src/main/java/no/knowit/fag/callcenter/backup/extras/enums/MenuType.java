package no.knowit.fag.callcenter.backup.extras.enums;

public enum MenuType {

    SPOKEN("spoken"),
    PLAYED("played");

    private final String value;

    MenuType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return  value;
    }
}
