package com.team44.isa_youtubeich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadEventJsonDto implements Serializable {
    private String videoTitle;
    private long fileSize;
    private String author;
    private long timestamp;
}