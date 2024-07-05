package com.maticz.ACPerformance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.maticz.ACPerformance.model.Campaign;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public interface ACService{

    String getACData() throws JsonProcessingException;

    JsonNode getCampaignID(String id, String pageNumber) throws JsonProcessingException;

    void saveCampaignDataToDB(String id) throws JsonProcessingException;

    JsonNode getClientsByCampaign(String idCampaign, String pageNumber) throws JsonProcessingException;

    HashMap<Integer, Integer> getDataForClientsFromCampaignsSentInPastWeek() throws JsonProcessingException;

    JsonNode getCampaingDataForLastWeek(String pageNumber) throws JsonProcessingException;

    List<Campaign> updateCampaignDataForLastWeek() throws JsonProcessingException;

    Integer getIdMessageForCampaignInDB(String id) throws JsonProcessingException;

    JsonNode getDataForContactsThatHaveNotOpenedEmail(String idCampaign, String idMessage, String pageNumber) throws JsonProcessingException;

    HashMap<Integer, Integer> saveContactsThatHaventOpenedEmail() throws JsonProcessingException;

    JsonNode getContactActivities(String idSubscriber) throws JsonProcessingException;

    JsonNode getContactActivitiesAfterDate(String idSubscriber, String date) throws JsonProcessingException;


    void saveContactEmailActivityAfterDate() throws JsonProcessingException;

    void saveContactDataForOpenedAndUnopenedEmails2() throws JsonProcessingException;

    HashMap<Integer, LocalDateTime> getDataForClientsThatOpenedCampaignsAndAddTimestampToMap() throws JsonProcessingException;

    HashMap<Integer, LocalDateTime> getDataForClientsThatOpenedCampaignsAndAddTimestampToMap2() throws JsonProcessingException;

    void saveContactsWhereDataIsMissingForPhotos() throws JsonProcessingException;


    void izbrisPol() throws JsonProcessingException;



}
