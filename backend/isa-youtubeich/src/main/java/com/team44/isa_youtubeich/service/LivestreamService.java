package com.team44.isa_youtubeich.service;

import java.time.LocalDateTime;

public interface LivestreamService {
    void schedulePremiere(Long videoId, LocalDateTime premieresAt);
    void startPremiere(Long videoId);
    void endPremiere(Long videoId);
    void cancelPremiere(Long videoId);
    void startPremiereEarly(Long videoId);
    String getHlsDirectory();
}
