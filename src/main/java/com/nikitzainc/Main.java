package com.nikitzainc;


import com.nikitzainc.service.impl.ApiScrapperService;
import com.nikitzainc.service.impl.FileWritterService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length != 4 || !(args[3].equals("csv") || args[3].equals("json"))) {
            throw new IllegalArgumentException(
                    "Wrong arguments! Correct usage: threadAmount, timeout, input file path, output file type[csv|json]");
        }
        int threadAmount = Integer.parseInt(args[0]);
        int timeout = Integer.parseInt(args[1]);
        String inputFilePath = args[2];
        boolean outputFileFormat = args[3].equals("csv");

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(threadAmount * 2);

        ApiScrapperService apiService = new ApiScrapperService(
                (int) Math.ceil(threadAmount/2.0), timeout, queue, inputFilePath);

        FileWritterService fileService = new FileWritterService(
                (int) Math.floor(threadAmount/2.0), queue, outputFileFormat);


        Thread apiThread = new Thread(() -> {
            try {
                apiService.start();
            } catch (Exception e) {
                System.out.println("API Service failed: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });

        Thread fileThread = new Thread(() -> {
            try {
                fileService.start();
            } catch (Exception e) {
                System.out.println("File Service failed: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });

        apiThread.start();
        fileThread.start();


        try {
            TimeUnit.SECONDS.sleep(25);
            apiService.stop();
            fileService.stop();

            apiThread.interrupt();
            apiThread.join();
            fileThread.interrupt();
            fileThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
