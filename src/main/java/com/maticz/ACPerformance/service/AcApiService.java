package com.maticz.ACPerformance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public interface AcApiService {

    JsonNode getUnsubscriptionsByCampaign(String idCampaign, String page) throws JsonProcessingException;

    JsonNode getContactActivities(String idSubscriber) throws JsonProcessingException;

    JsonNode getDataForContactsThatHaveNotOpenedEmail(String idCampaign, String idMessage, String pageNumber) throws JsonProcessingException;

    JsonNode getLinkData(String idCampaign) throws JsonProcessingException;

    JsonNode getMPPLinkData(String idLink) throws JsonProcessingException;}
