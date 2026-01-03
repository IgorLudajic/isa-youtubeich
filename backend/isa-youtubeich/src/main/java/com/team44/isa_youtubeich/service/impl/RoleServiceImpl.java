package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.Role;
import com.team44.isa_youtubeich.repository.RoleRepository;
import com.team44.isa_youtubeich.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role findById(Long id) {
        return this.roleRepository.getReferenceById(id);
    }

    @Override
    public List<Role> findByName(String name) {
        return this.roleRepository.findByName(name);
    }
}
