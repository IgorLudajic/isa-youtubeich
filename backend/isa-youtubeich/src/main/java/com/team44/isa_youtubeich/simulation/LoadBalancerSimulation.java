package com.team44.isa_youtubeich.simulation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadBalancerSimulation {

    private static final String BASE_URL = "http://localhost:8080/api/videos";
    private static final Long VIDEO_ID = 1L;
    private static final int USER_COUNT = 50;

    private static final int SERVER_WAIT_TIMEOUT_SEC = 60;

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("INICIJALIZACIJA...");

        if (!waitForServer()) {
            System.err.println("Server se nije podigao na vreme. Prekidam simulaciju.");
            return;
        }

        System.out.println("SERVER JE SPREMAN! POČINJEM SIMULACIJU OPTEREĆENJA NA PORT 8080...");
        System.out.println("Cilj: Proveriti da li se zahtevi raspoređuju na obe replike.");

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

    private static boolean waitForServer() {
        System.out.println("Proveravam da li je server živ (pingujem Nginx)...");
        String testUrl = BASE_URL + "/" + VIDEO_ID + "/thumbnail";

        long start = System.currentTimeMillis();

        while ((System.currentTimeMillis() - start) < (SERVER_WAIT_TIMEOUT_SEC * 1000)) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(testUrl))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    System.out.println("Server je odgovorio (200 OK)! Spreman za akciju.");
                    return true;
                } else {
                    System.out.print(".");
                }
            } catch (Exception e) {
                System.out.print("x");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
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
                System.out.println("[User " + userId + "]Uspešan pregled! (" + duration + "ms)");
            } else {
                System.out.println("[User " + userId + "] Greška: " + response.statusCode());
            }

        } catch (Exception e) {
            System.out.println("[User " + userId + "] Pukla konekcija: " + e.getMessage());
        }
    }
}