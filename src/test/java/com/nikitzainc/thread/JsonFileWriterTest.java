package com.nikitzainc.thread;

import com.nikitzainc.thread.impl.CsvFileWriter;
import com.nikitzainc.thread.impl.JsonFileWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonFileWriterTest {
    private BlockingQueue<String> mockQueue;
    private File testFile;
    private ObjectMapper mapper = new ObjectMapper();
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        mockQueue = mock(BlockingQueue.class);
        testFile = tempDir.resolve("test.json").toFile();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() throws Exception {
        testFile.delete();
        System.setOut(standardOut);
    }

    @Test
    void testWriteToJson() throws Exception {
        String json1 = "{\"data\":[\"That's an example json about cats\"]}";
        String json2 = "{\"data\":[\"That's another example json about cats\"]}";

        ArrayNode array = mapper.createArrayNode();
        array.add(mapper.readTree(json1));
        mapper.writeValue(testFile, array);

        when(mockQueue.take()).thenReturn(json2);
        JsonFileWriter writer = new JsonFileWriter(testFile, mockQueue);

        writer.run();
        ArrayNode resultArray = (ArrayNode) mapper.readTree(testFile);
        assertAll(
                () -> assertEquals(2, resultArray.size()),
                () -> assertEquals(json1, resultArray.get(0).toString()),
                () -> assertEquals(json2, resultArray.get(1).toString()));
    }

    @Test
    void testBadWriteToJson() throws Exception {
        String json = "{\"data:\"That's some goofy aah json string ";
        when(mockQueue.take()).thenReturn(json);

        JsonFileWriter writer = new JsonFileWriter(testFile, mockQueue);
        writer.run();

        assertEquals(0, testFile.length());
        assertTrue(outputStreamCaptor.toString().trim().contains("Error writing to file:"));
    }

    @Test
    void testEmptyQueueHandling() throws Exception {
        when(mockQueue.take()).thenThrow(new InterruptedException());

        JsonFileWriter writer = new JsonFileWriter(testFile, mockQueue);

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