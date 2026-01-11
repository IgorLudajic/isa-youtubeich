package com.team44.isa_youtubeich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheablePagedResponse<T> {
    List<T> content;
    int number;
    long totalElements;
    int totalPages;
    int size;
    boolean last;
    boolean first;
}