package com.nikitzainc.thread;

import com.nikitzainc.thread.impl.CsvFileWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CsvFileWriterTest {
    private BlockingQueue<String> mockQueue;
    private File testFile;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        mockQueue = mock(BlockingQueue.class);
        testFile = tempDir.resolve("test.csv").toFile();
    }

    @Test
    void testSimpleWriteToCsv() throws Exception {
        String json = "{\"data\":\"That's an example json about cats\"}";
        String outputString = "That's an example json about cats";
        when(mockQueue.take()).thenReturn(json);

        CsvFileWriter writer = new CsvFileWriter(testFile, mockQueue);

        writer.run();
        assertAll(
                () -> testFile.exists(),
                () -> assertLinesMatch(Collections.singletonList(outputString), Files.readAllLines(testFile.toPath())));
    }

    @Test
    void testArrayWriteToCsv() throws Exception {
        String json = "{\"data\":[\"That's an example json about cats\",\"Thats second example json about cats\"]}";
        String outputString = "That's an example json about cats, Thats second example json about cats";
        when(mockQueue.take()).thenReturn(json);
        CsvFileWriter writer = new CsvFileWriter(testFile, mockQueue);

        writer.run();

        assertAll(
                () -> testFile.exists(),
                () -> assertLinesMatch(Collections.singletonList(outputString), Files.readAllLines(testFile.toPath())));
    }

    @Test
    void testBadWriteToCsv() throws Exception {
        String json = "{\"data:\"That's some goofy aah json string ";
        when(mockQueue.take()).thenReturn(json);
        CsvFileWriter writer = new CsvFileWriter(testFile, mockQueue);

        writer.run();

        assertEquals(0, testFile.length());
    }

    @Test
    void testEmptyQueueHandling() throws Exception {
        when(mockQueue.take()).thenThrow(new InterruptedException());
        CsvFileWriter writer = new CsvFileWriter(testFile, mockQueue);

        writer.run();

        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testSetFile() {
        CsvFileWriter writer = new CsvFileWriter(testFile, mockQueue);
        File newFile = new File("newFile");

        writer.setFile(newFile);

        assertEquals(newFile, writer.getFile());
    }

    @Test
    void testSetQueue() {
        CsvFileWriter writer = new CsvFileWriter(testFile, mockQueue);
        ArrayBlockingQueue<String> testqueue = new ArrayBlockingQueue<>(1);

        writer.setResponseQueue(testqueue);

        assertEquals(testqueue, writer.getResponseQueue());
    }
}
