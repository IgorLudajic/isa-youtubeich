package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.CacheablePagedResponse;
import com.team44.isa_youtubeich.dto.CommentRequestDto;
import com.team44.isa_youtubeich.dto.CommentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CacheablePagedResponse<CommentResponseDto> getVideoComments(Long videoId, Pageable pageable);
    CommentResponseDto postComment(Long videoId, CommentRequestDto requestBody, String username);
}
