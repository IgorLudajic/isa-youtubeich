package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.dto.CommentResponseDto;
import com.team44.isa_youtubeich.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/video/{videoId}")
    public ResponseEntity<Page<CommentResponseDto>> getVideoComments(@PathVariable Long id, Pageable pageable){
        return ResponseEntity.ok(commentService.getVideoComments(id, pageable));
    }

    @PostMapping("/video/{videoId}")
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable Long videoId, @RequestBody String text, Principal principal){
        return ResponseEntity.ok(commentService.postComment(videoId, text, principal.getName()));
    }
}
