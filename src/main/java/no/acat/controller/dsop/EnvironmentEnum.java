package no.acat.controller.dsop;

public enum EnvironmentEnum {
    TEST("test"),
    PRODUCTION("production"),
    UNDEFINED("undefined");

    public final String value;

    EnvironmentEnum(String value) {
        this.value = value;
    }

    public static EnvironmentEnum fromStringValue(String value) {
        if(value == null) {
            return UNDEFINED;
        } else if(value.toLowerCase().equals("test")) {
            return TEST;
        } else if (value.toLowerCase().equals("production")) {
            return PRODUCTION;
        } else {
            return UNDEFINED;
        }
    }
}
