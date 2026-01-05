package com.team44.isa_youtubeich.service.internal;

import com.team44.isa_youtubeich.domain.model.User;

import java.util.List;

public interface InternalUserService {
    User findByUsername(String username);
}
