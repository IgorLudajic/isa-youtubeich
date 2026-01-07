package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.CommentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    Page<CommentResponseDto> getVideoComments(Long videoId, Pageable pageable);
    CommentResponseDto postComment(Long videoId, String text, String username);
}
