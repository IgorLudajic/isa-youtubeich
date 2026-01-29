package com.team44.isa_youtubeich.crdt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class GCounterPerformanceTest {

    @Autowired
    private GCounter gCounter;

    @Test
    public void testHighThroughputIncrement() throws InterruptedException {
        int numThreads = 10;
        long totalIncrements = 100_000; // 100k increments
        long incrementsPerThread = totalIncrements / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        long startTime = System.nanoTime();

        for (int t = 0; t < numThreads; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    for (long i = 0; i < incrementsPerThread; i++) {
                        gCounter.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        doneLatch.await(); // Wait for all threads to finish

        // Allow time for async message processing
        Thread.sleep(2000);

        long endTime = System.nanoTime();
        double durationSeconds = (endTime - startTime) / 1e9;
        double throughput = totalIncrements / durationSeconds;

        System.out.println("Total increments: " + totalIncrements);
        System.out.println("Duration: " + durationSeconds + " seconds");
        System.out.println("Throughput: " + throughput + " ops/s");

        // Verify the counter value (allowing some tolerance for async processing)
        long finalValue = gCounter.getValue();
        System.out.println("Final counter value: " + finalValue);
        assertEquals(totalIncrements, finalValue, totalIncrements * 0.01, "Counter value should match total increments within 1% tolerance");

        // Assert throughput meets the requirement
//        assert throughput > 10_000 : "Throughput should exceed 10k ops/s, got: " + throughput;

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
