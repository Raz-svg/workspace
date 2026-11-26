package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void greetUsesProvidedName() {
        assertEquals("Hello, Gradle!", App.greet("Gradle"));
    }

    @Test
    void greetFallsBackToWorldWhenBlank() {
        assertEquals("Hello, World!", App.greet("   "));
        assertEquals("Hello, World!", App.greet(null));
    }
}
