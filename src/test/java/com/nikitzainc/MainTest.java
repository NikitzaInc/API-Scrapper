package com.nikitzainc;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @Test
    void testCorrectArguments() {
        String[] args = {"10", "10", "src\\main\\resources\\apiConfig.json", "csv"};

        assertDoesNotThrow(() -> Main.main(args));
    }

    @Test
    void testInvalidArguments() {
        String[] args = {"10", "10", "src\\main\\resources\\apiConfig.json", "badtype"};

        assertThrows(IllegalArgumentException.class, () -> Main.main(args));
    }

    @Test
    void testInvalidAmountOfArguments() {
        String[] args = {"10", "src\\main\\resources\\apiConfig.json", "csv"};

        assertThrows(IllegalArgumentException.class, () -> Main.main(args));
    }

    @Test
    void testServicesFailure() {
        System.setOut(new PrintStream(outputStreamCaptor));
        String[] args = {"10", "-1", "src\\main\\resources\\apiConfig.json", "csv"};

        Main.main(args);

        assertTrue(outputStreamCaptor.toString().trim().contains("API Service failed:"));
        System.setOut(standardOut);
    }





}