package com.pluralsight.cli;

public class console {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void Success(String message, Object... args) {
        System.out.printf(ANSI_GREEN + message + ANSI_RESET + "%n", args);
    }
    public static void Warning(String message, Object... args) {
        System.out.printf(ANSI_YELLOW + message + ANSI_RESET + "%n", args);
    }
    public static void Deny(String message, Object... args) {
        System.out.printf(ANSI_RED + message + ANSI_RESET + "%n", args);
    }
    public static void Information(String message, Object... args) {
        System.out.printf(ANSI_BLUE + message + ANSI_RESET + "%n", args);
    }


    public static void LabelSuccess(String message, Object... args) {
        System.out.printf(ANSI_GREEN + "[Success] " + message + ANSI_RESET + "%n", args);
    }
    public static void LabelWarning(String message, Object... args) {
        System.out.printf(ANSI_YELLOW + "[Warning] " + message + ANSI_RESET + "%n", args);
    }
    public static void LabelDeny(String message, Object... args) {
        System.out.printf(ANSI_RED + "[Deny] " + message + ANSI_RESET + "%n", args);
    }
    public static void LabelInformation(String message, Object... args) {
        System.out.printf(ANSI_BLUE + "[Information] " + message + ANSI_RESET + "%n", args);
    }

}
