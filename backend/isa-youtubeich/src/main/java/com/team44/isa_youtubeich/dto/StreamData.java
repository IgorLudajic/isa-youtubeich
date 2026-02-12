package com.team44.isa_youtubeich.dto;

public class StreamData {
    private byte[] content;
    private String contentType;

    public StreamData(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }
}
