package no.knowit.fag.callcenter.backup.extras.enums;

public enum CallType {

    CONFERENCE("conference"),
    QUEUE("queue");

    private final String value;

    CallType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return  value;
    }
}
