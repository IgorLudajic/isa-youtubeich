package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.service.BenchmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    @Autowired
    private BenchmarkService benchmarkService;

    @GetMapping("/run")
    public String run() {
        return benchmarkService.runBenchmark();
    }
}