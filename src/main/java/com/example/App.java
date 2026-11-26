
package com.example;

public class App {

    /**
     * Returns a greeting message for the given target.
     *
     * @param name recipient name, or blank/null to fall back to "World"
     * @return formatted greeting
     */
    public static String greet(String name) {
        String target = (name == null || name.isBlank()) ? "World" : name.trim();
        return "Hello, " + target + "!";
    }

    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "Java " + Runtime.version().feature();
        System.out.println(greet(name));
    }
}
