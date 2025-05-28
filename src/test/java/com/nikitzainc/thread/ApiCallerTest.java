package com.nikitzainc.thread;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ApiCallerTest {
    private CloseableHttpClient mockHttpClient;
    private CloseableHttpResponse mockResponse;
    private BlockingQueue<String> mockQueue;
    private StatusLine mockStatusLine;
    private HttpEntity mockEntity;

    @InjectMocks
    private ApiCaller apiCaller;

    @BeforeEach
    void setUp() throws IOException {
        mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        mockResponse = Mockito.mock(CloseableHttpResponse.class);
        mockQueue = Mockito.mock(BlockingQueue.class);
        mockStatusLine = Mockito.mock(StatusLine.class);
        mockEntity = Mockito.mock(HttpEntity.class);

        when(mockHttpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
    }

    @Test
    void testSuccessfulApiCall() throws Exception {
        String testUrl = "http://test.com";
        String responseBody = "{\"data\":[\"That's an example json about cats\"]}";

        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        apiCaller = new ApiCaller(testUrl, mockQueue);
        apiCaller.setHttpClient(mockHttpClient);

        apiCaller.run();
        verify(mockQueue).put(responseBody);
    }

    @Test
    void testFailedApiCall() throws Exception {
        String testUrl = "http://test.com";

        when(mockStatusLine.getStatusCode()).thenReturn(404);
        ApiCaller apiCaller = new ApiCaller(testUrl, mockQueue);
        apiCaller.setHttpClient(mockHttpClient);

        apiCaller.run();
        verify(mockQueue, never()).put(any());
    }

    @Test
    void testIOExceptionHandling() throws Exception {
        String testUrl = "http://test.com";

        when(mockHttpClient.execute(any(HttpGet.class))).thenThrow(new IOException("Connection failed"));

        ApiCaller apiCaller = new ApiCaller(testUrl, mockQueue);
        apiCaller.setHttpClient(mockHttpClient);

        apiCaller.run();
        verify(mockQueue, never()).put(any());
    }

    @Test
    void testInterruptedExceptionHandling() throws Exception {
        String testUrl = "http://test.com";
        String responseBody = "{\"data\":[\"That's an example json about cats\"]}";
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));
        doThrow(new InterruptedException()).when(mockQueue).put(any());
        ApiCaller apiCaller = new ApiCaller(testUrl, mockQueue);
        apiCaller.setHttpClient(mockHttpClient);

        Thread testThread = new Thread(apiCaller::run);
        testThread.start();
        Thread.sleep(1500);

        assertTrue(testThread.isInterrupted());
    }
}
