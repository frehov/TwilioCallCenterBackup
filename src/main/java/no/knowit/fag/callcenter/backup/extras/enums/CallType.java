package no.knowit.fag.callcenter.backup.extras.enums;

public enum CallType {

    dial("dial"),
    conference("conference"),
    queue("queue");

    private final String value;

    CallType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return  value;
    }
}
