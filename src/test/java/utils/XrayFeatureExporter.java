package utils;
import org.junit.runner.JUnitCore;
import steps.RunnerTest;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
public class XrayFeatureExporter {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Veuillez entrer le testPlanKey : ");
        String testPlanKey = scanner.nextLine();

        // 1. Génération du token
        String token = generateToken();
        System.out.println("Token généré.\n");

        // 2. Importation des features depuis Xray
        Path featuresDir = Paths.get("src/test/resources/features/imported");
        Files.createDirectories(featuresDir);
        downloadAndExtractFeatures(token, testPlanKey, featuresDir);

        // 3. Fusion des features (optionnel mais pratique)
        Path mergedFeature = Paths.get("src/test/resources/features/feature.feature");
        mergeFeatures(featuresDir, mergedFeature);

        // 4. Exécution des tests
        System.out.println("Exécution des tests...");
        JUnitCore.runClasses(RunnerTest.class);

        // 5. Export des résultats à Xray
        System.out.println("Export des résultats...");
        exportResultsToXray(token);
    }

    private static String generateToken() throws IOException, InterruptedException {
        String clientId = "E577F9CAEC8A4171985D2F82F129D73F";
        String clientSecret = "fe3003afe6ef037751c6e1f8321f8145f9f01b274b0ab43bbb609aad7759957a";

        String body = String.format("{\"client_id\":\"%s\",\"client_secret\":\"%s\"}", clientId, clientSecret);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://xray.cloud.getxray.app/api/v2/authenticate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body().replace("\"", "");
    }

    private static void downloadAndExtractFeatures(String token, String testPlanKey, Path extractDir) throws IOException, InterruptedException {
        String url = "https://xray.cloud.getxray.app/api/v2/export/cucumber?keys=" + testPlanKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<byte[]> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            byte[] zipData = response.body();
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path filePath = extractDir.resolve(entry.getName());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            System.out.println("📦 Fichiers feature importés et extraits.");
        } else {
            throw new IOException("Erreur HTTP Xray : " + response.statusCode());
        }
    }

    private static void mergeFeatures(Path sourceDir, Path outputFile) throws IOException {
        StringBuilder merged = new StringBuilder("Feature: Test Xray\n\n");

        Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".feature"))
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        content = content.replaceAll("(?i)^Feature:.*\\R", ""); // remove duplicate "Feature:" lines
                        merged.append(content).append("\n\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        Files.writeString(outputFile, merged.toString(), StandardCharsets.UTF_8);
        System.out.println("Fichier fusionné sauvegardé dans : " + outputFile);
    }

    private static void exportResultsToXray(String token) throws IOException, InterruptedException {
        Path reportPath = Paths.get("target/cucumber.json");

        if (!Files.exists(reportPath)) {
            System.err.println("Le fichier JSON de résultats est introuvable !");
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://xray.cloud.getxray.app/api/v2/import/execution/cucumber"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(reportPath))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Résultats correctement envoyés à Xray.");
        } else {
            System.err.println("Erreur lors de l'envoi des résultats : " + response.statusCode());
            System.err.println(response.body());
        }
    }
}
