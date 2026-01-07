package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.Comment;
import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.CommentResponseDto;
import com.team44.isa_youtubeich.exception.ResourceConflictException;
import com.team44.isa_youtubeich.repository.CommentRepository;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.CommentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponseDto postComment(Long videoId, String text, String username){
        User user = userRepository.findByUsername(username);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new ResourceConflictException(videoId, "Video not found"));

        Comment comment = new Comment();
        comment.setText(text);
        comment.setUser(user);
        comment.setVideo(video);
        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Comment saved = commentRepository.save(comment);

        return new CommentResponseDto(
                saved.getId(),
                saved.getText(),
                Date.from(saved.getCreatedAt().toInstant()),
                user.getUsername()
        );
    }

    @Override
    public Page<CommentResponseDto> getVideoComments(Long videoId, Pageable pageable){
        return commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId, pageable)
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getText(),
                        Date.from(comment.getCreatedAt().toInstant()),
                        comment.getUser().getUsername()
                ));
    }
}
