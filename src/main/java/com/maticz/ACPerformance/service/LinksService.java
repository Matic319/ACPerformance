package com.maticz.ACPerformance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

@Service
public interface LinksService {

    public void saveLinkIdToDB(String idCampaign, String idMessage) throws JsonProcessingException;
}
