package no.acat.controller.dsop;

public enum EnvironmentEnum {
    TEST,
    PRODUCTION,
    UNDEFINED;

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
