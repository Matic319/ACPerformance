package com.maticz.ACPerformance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public interface UnsubscriptionService {


    void saveUnsubscriptionsToDB() throws JsonProcessingException;
}
