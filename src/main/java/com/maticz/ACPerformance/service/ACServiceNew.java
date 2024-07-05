package com.maticz.ACPerformance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface ACServiceNew {

    void getDataFromACAndSaveToDB() throws JsonProcessingException;

    LocalDateTime getTimestampForContactThatIsNotInMap(String idSubscriber, Integer idCampaign) throws JsonProcessingException;

}
