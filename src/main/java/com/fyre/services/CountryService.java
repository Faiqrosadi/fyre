package com.fyre.services;

import com.fyre.models.University;
import com.fyre.repositories.UnivesityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryService {

    @Autowired
    UnivesityRepository univesityRepository;

    public List<University> getAllUniversities() {
        return univesityRepository.findAll();
    }

    public University getCountryByCode(String id) {
        return univesityRepository.findById(id);
    }
}
