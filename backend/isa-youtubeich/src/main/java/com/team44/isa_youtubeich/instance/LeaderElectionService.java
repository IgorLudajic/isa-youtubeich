package com.team44.isa_youtubeich.instance;

import java.util.UUID;

public interface LeaderElectionService {
    boolean isLeader();
    UUID getCurrentLeaderId();
}
