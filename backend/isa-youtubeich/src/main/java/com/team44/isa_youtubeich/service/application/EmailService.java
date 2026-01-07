package com.team44.isa_youtubeich.service.application;

import com.team44.isa_youtubeich.domain.model.User;

public interface EmailService {
    void sendActivationEmail(User user, String activationToken);
}

