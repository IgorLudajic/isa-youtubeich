package com.team44.isa_youtubeich.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path hlsDir = Paths.get("uploads", "hls").toAbsolutePath().normalize();
        String hlsLocation = hlsDir.toUri().toString();

        registry.addResourceHandler("/uploads/hls/**")
                .addResourceLocations(hlsLocation)
                // Playlist must not be cached; segments can be cached, but keeping it simple for now.
                .setCacheControl(CacheControl.noCache());

        // NOTE: We intentionally do NOT expose /uploads/videos/** as a static resource.
        // MP4 access must go through VideoController so we can enforce VideoStatus.ENDED.
        // (HLS segments remain accessible under /uploads/hls/**.)
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // Make sure HLS/MP4 are served with correct Content-Type.
        configurer.mediaType("m3u8", MediaType.valueOf("application/vnd.apple.mpegurl"));
        configurer.mediaType("ts", MediaType.valueOf("video/mp2t"));
        configurer.mediaType("mp4", MediaType.valueOf("video/mp4"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/uploads/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Accept-Ranges", "Content-Range", "Content-Length")
                .allowCredentials(false)
                .maxAge(3600);

        registry.addMapping("/api/videos/*/stream")
                .allowedOriginPatterns("*")
                .allowedMethods(HttpMethod.GET.name(), HttpMethod.HEAD.name(), HttpMethod.OPTIONS.name())
                .allowedHeaders("*")
                .exposedHeaders("Accept-Ranges", "Content-Range", "Content-Length")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
