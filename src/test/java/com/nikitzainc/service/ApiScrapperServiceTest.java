package com.nikitzainc.service;

import com.nikitzainc.service.impl.ApiScrapperService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiScrapperServiceTest {
    private final BlockingQueue<String> mockQueue = mock(BlockingQueue.class);
    private final ExecutorService mockExecutor = mock(ExecutorService.class);
    private ApiScrapperService service;
    private List<String> testApis;

    @TempDir
    Path tempDir;
    private String testFilePath;

    @BeforeEach
    void setUp() throws IOException {
        testApis = new ArrayList<>();
        testApis.add("http://api1.com");
        testApis.add("http://api2.com");
        testFilePath = tempDir.resolve("test_path.json").toString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(testFilePath), testApis);
    }

    @Test
    void testConstructorValidParameters() {
        service = new ApiScrapperService(2, 1, mockQueue, testFilePath);
        service.setExecutor(mockExecutor);

        assertEquals(service.getExecutor(), mockExecutor);
        assertTrue(service.isActive());
    }

    @Test
    void testConstructorInvalidParameters() {
        testFilePath = tempDir.resolve("test_path_invalid.json").toString();

        assertThrows(RuntimeException.class,
                () -> service = new ApiScrapperService(2, 1, mockQueue, testFilePath));
    }

    @Test
    void testStartServiceMakesApiCalls() throws Exception {
        service = new ApiScrapperService(2, 1, mockQueue, testFilePath);
        service.setExecutor(mockExecutor);

        Thread testThread = new Thread(() -> {
            try {
                service.start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        testThread.start();
        Thread.sleep(1500);
        service.stop();
        testThread.join();

        assertFalse(mockQueue.isEmpty());
    }

    @Test
    void testStopServiceShutsDownExecutor() throws InterruptedException {
        service = new ApiScrapperService(2, 1, mockQueue, testFilePath);
        service.setExecutor(mockExecutor);

        service.stop();

        verify(mockExecutor).shutdown();
        assertFalse(service.isActive());
    }

    @Test
    void testSetFile() {
        service = new ApiScrapperService(2, 1, mockQueue, testFilePath);

        service.setExecutor(mockExecutor);

        assertEquals(mockExecutor, service.getExecutor());
    }
}