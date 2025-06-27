package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the NPCService interface.
 */
@Service
public class NPCServiceImpl implements NPCService {

    private final NPCRepository npcRepository;

    @Autowired
    public NPCServiceImpl(NPCRepository npcRepository) {
        this.npcRepository = npcRepository;
    }

    @Override
    public NPC findById(Long id) {
        return npcRepository.findById(id)
                .orElse(null);
    }

    @Override
    public NPC findByName(String name) {
        return npcRepository.findByName(name)
                .orElse(null);
    }

    @Override
    public List<NPC> findAll() {
        return npcRepository.findAll();
    }

    @Override
    public List<NPC> findByType(NPC.NPCType type) {
        return npcRepository.findByType(type);
    }

    @Override
    public NPC save(NPC npc) {
        return npcRepository.save(npc);
    }

    @Override
    public void delete(Long id) {
        npcRepository.deleteById(id);
    }
}