package com.nikitzainc.service;

import com.nikitzainc.service.impl.FileWritterService;
import com.nikitzainc.thread.impl.CsvFileWriter;
import com.nikitzainc.thread.impl.JsonFileWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

class FileWritterServiceTest {
    private BlockingQueue<String> mockQueue = mock(BlockingQueue.class);
    private ExecutorService mockExecutor = mock(ExecutorService.class);
    private FileWritterService csvService;
    private FileWritterService jsonService;

    @BeforeEach
    void setUp() {
        csvService = new FileWritterService(2, mockQueue, true);
        csvService.setExecutor(mockExecutor);
        jsonService = new FileWritterService(2, mockQueue, false);
        jsonService.setExecutor(mockExecutor);

        doNothing().when(mockExecutor).execute(any(Runnable.class));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        csvService.stop();
        jsonService.stop();
    }

    @Test
    void testCsvWriterStart() throws Exception {
        when(mockQueue.isEmpty()).thenReturn(false).thenReturn(true);

        Thread testThread = new Thread(() -> {
            try {
                csvService.start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        testThread.start();
        Thread.sleep(1500);
        csvService.stop();
        testThread.join();

        verify(csvService.getExecutor()).execute(any(CsvFileWriter.class));
    }

    @Test
    void testJsonWriterStart() throws Exception {
        when(mockQueue.isEmpty()).thenReturn(false).thenReturn(true);

        Thread testThread = new Thread(() -> {
            try {
                jsonService.start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        testThread.start();
        Thread.sleep(1500);
        jsonService.stop();
        testThread.join();

        verify(jsonService.getExecutor()).execute(any(JsonFileWriter.class));
    }

    @Test
    void testEmptyQueueHandling() throws InterruptedException {
        when(mockQueue.isEmpty()).thenReturn(true);

        Thread testThread = new Thread(() -> {
            try {
                csvService.start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        testThread.start();
        Thread.sleep(1500);
        csvService.stop();
        testThread.join();

        verify(csvService.getExecutor(), never()).execute(any());
    }
}