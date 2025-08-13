package com.ds.server.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SummarizerService {

    @Value("${cohere.api.key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String summarize(String text) throws Exception {

        String safeText = text
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        String requestBody = """
        {
          "model": "summarize-xlarge",
          "text": "%s"
        }
        """.formatted(safeText);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cohere.ai/v1/summarize"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public String extractTextFromFile(MultipartFile file) throws Exception {
        String filename=file.getOriginalFilename().toLowerCase();

        try(InputStream inputStream=file.getInputStream()) {
            if(filename.endsWith(".pdf")){
                PDDocument document = PDDocument.load(inputStream);
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                document.close();
                return text;
            }else if(filename.endsWith(".docx")){
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                String text = extractor.getText();
                document.close();
                return text;
            }else if(filename.endsWith(".txt")) {
                return new String(file.getBytes());
            }else{
                throw new IllegalArgumentException("Filename not supported");
            }
        }
    }
}
