package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.dto.CacheablePagedResponse;
import com.team44.isa_youtubeich.dto.CommentRequestDto;
import com.team44.isa_youtubeich.dto.CommentResponseDto;
import com.team44.isa_youtubeich.service.CommentService;
import jakarta.websocket.server.PathParam;
import org.hibernate.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/video/{videoId}")
    public ResponseEntity<CacheablePagedResponse<CommentResponseDto>> getVideoComments(@PathVariable Long videoId, Pageable pageable){
        return ResponseEntity.ok(commentService.getVideoComments(videoId, pageable));
    }

    @PostMapping("/video/{videoId}")
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable Long videoId, @RequestBody CommentRequestDto body, Principal principal){
        return ResponseEntity.ok(commentService.postComment(videoId, body, principal.getName()));
    }
}
