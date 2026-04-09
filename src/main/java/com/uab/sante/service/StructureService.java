package com.uab.sante.service;

import com.uab.sante.entities.Structure;
import com.uab.sante.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StructureService {

    private final StructureRepository structureRepository;

    public List<Structure> getAll() {
        return structureRepository.findAll();
    }

    public Structure getById(Long id) {
        return structureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
    }

    public List<Structure> getByType(Structure.TypeStructure type) {
        return structureRepository.findByTypeAndActifTrue(type);
    }

    @Transactional
    public Structure create(Structure structure) {
        if (structureRepository.findByCodeStructure(structure.getCodeStructure()).isPresent()) {
            throw new RuntimeException("Une structure avec ce code existe déjà");
        }
        return structureRepository.save(structure);
    }

    @Transactional
    public Structure update(Structure structure) {
        return structureRepository.save(structure);
    }

    @Transactional
    public void desactiver(Long id) {
        Structure structure = getById(id);
        structure.setActif(false);
        structureRepository.save(structure);
    }

    @Transactional
    public void activer(Long id) {
        Structure structure = getById(id);
        structure.setActif(true);
        structureRepository.save(structure);
    }
}
