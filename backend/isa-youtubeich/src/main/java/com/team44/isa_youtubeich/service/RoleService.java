package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.domain.model.Role;

import java.util.List;

public interface RoleService {
    Role findById(Long id);
    List<Role> findByName(String name);
}
