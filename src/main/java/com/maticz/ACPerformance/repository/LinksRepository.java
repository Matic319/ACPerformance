package com.maticz.ACPerformance.repository;

import com.maticz.ACPerformance.model.Links;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinksRepository extends JpaRepository<Links,Integer> {
}
