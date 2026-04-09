package com.uab.sante.service;

import com.uab.sante.entities.Assure;
import com.uab.sante.repository.AssureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssureService {

    private final AssureRepository assureRepository;

    public Optional<Assure> findByTelephone(String telephone) {
        return assureRepository.findByTelephone(telephone);
    }

    public Optional<Assure> findByNumeroPolice(String numeroPolice) {
        return assureRepository.findByNumeroPolice(numeroPolice);
    }

    public Assure save(Assure assure) {
        return assureRepository.save(assure);
    }
}
