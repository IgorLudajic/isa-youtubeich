package com.team44.isa_youtubeich.service;

public interface VideoViewService {

    void enqueueView(Long videoId, String username);

    Long getViewCount(Long videoId);

}
