package com.ds.server.controller;

import com.ds.server.service.SummarizerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/summarize")
public class SummarizerController {

    private final SummarizerService summarizerService;

    public SummarizerController(SummarizerService summarizerService) {
        this.summarizerService = summarizerService;
    }

    @PostMapping()
    public String summarizeFromFile(@RequestParam("file") MultipartFile file) throws Exception {
        String text = summarizerService.extractTextFromFile(file);
        String summary= summarizerService.summarize(text);
        return summary;
    }
}
