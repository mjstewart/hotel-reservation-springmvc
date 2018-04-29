package com.demo.reservation;

import com.demo.domain.Extra;
import com.demo.persistance.HotelRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ExtraRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExtraRepository extraRepository;

    @Test
    public void findAllByTypeAndCategory() {
        entityManager.persist(new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.General));
        entityManager.persist(new Extra("b", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food));
        entityManager.persist(new Extra("c", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food));
        entityManager.persist(new Extra("d", BigDecimal.valueOf(1.50), Extra.Type.Premium, Extra.Category.Food));
        entityManager.persist(new Extra("f", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.General));

        List<Extra> results = extraRepository.findAllByTypeAndCategory(Extra.Type.Basic, Extra.Category.General);
        assertThat(results).extracting(Extra::getDescription).containsOnly("a", "f");

        results = extraRepository.findAllByTypeAndCategory(Extra.Type.Basic, Extra.Category.Food);
        assertThat(results).extracting(Extra::getDescription).containsOnly("b", "c");

        results = extraRepository.findAllByTypeAndCategory(Extra.Type.Premium, Extra.Category.General);
        assertThat(results).isEmpty();

        results = extraRepository.findAllByTypeAndCategory(Extra.Type.Premium, Extra.Category.Food);
        assertThat(results).extracting(Extra::getDescription).containsOnly("d");
    }
}