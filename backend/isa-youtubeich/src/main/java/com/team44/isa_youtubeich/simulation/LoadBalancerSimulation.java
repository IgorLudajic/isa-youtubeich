package com.team44.isa_youtubeich.simulation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadBalancerSimulation {

    private static final String BASE_URL = "http://localhost:8080/api/videos";
    private static final Long VIDEO_ID = 1L;

    private static final int USER_COUNT = 50;

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("POČINJEM SIMULACIJU OPTEREĆENJA NA PORT 8080 (NGINX)...");
        System.out.println("Cilj: Proveriti da li se zahtevi raspoređuju na obe replike (Gledaj Docker logove!)");

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < USER_COUNT; i++) {
            int userId = i;
            executor.submit(() -> simulateUserView(userId));

            Thread.sleep(100);
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("SIMULACIJA ZAVRŠENA.");
    }

    private static void simulateUserView(int userId) {
        try {
            String url = BASE_URL + "/" + VIDEO_ID + "/view";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("Content-Type", "application/json")
                    .build();

            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = System.currentTimeMillis() - startTime;

            if (response.statusCode() == 200) {
                System.out.println("[User " + userId + "] Uspešan pregled! (" + duration + "ms)");
            } else {
                System.out.println("[User " + userId + "] Greška: " + response.statusCode());
            }

        } catch (Exception e) {
            System.out.println("[User " + userId + "] Pukla konekcija: " + e.getMessage());
        }
    }
}