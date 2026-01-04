package com.team44.isa_youtubeich.service.internal;

import com.team44.isa_youtubeich.domain.model.Role;

import java.util.List;

public interface InternalRoleService {
    Role findById(Long id);
    List<Role> findByName(String name);
}
