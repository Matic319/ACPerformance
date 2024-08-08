package com.maticz.ACPerformance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.maticz.ACPerformance.model.Links;
import com.maticz.ACPerformance.repository.LinksRepository;
import com.maticz.ACPerformance.service.LinksService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class LinksServiceImpl implements LinksService {

    @Autowired
    AcApiServiceImpl acApiService;

    @Autowired
    LinksRepository linksRepository;


    @Override
    public void saveLinkIdToDB(String idCampaign, String idMessage) throws JsonProcessingException {
        //lahko je več "open" za različne idMessage
        JsonNode node = acApiService.getLinkData(idCampaign);
        for(JsonNode data : node.path("links")){
            Integer campaignId = data.path("campaignid").asInt();
            Integer messageId = data.path("messageid").asInt();
            String link = data.path("link").asText();
            Integer linkId = data.path("id").asInt();

            if(Objects.equals(String.valueOf(campaignId), idCampaign) &&
                    Objects.equals(String.valueOf(messageId), idMessage) &&
                    Objects.equals(link, "open")
            ){
                Links linkEntity = new Links();
                linkEntity.setLink(link);
                linkEntity.setIdCampaign(campaignId);
                linkEntity.setIdMessage(messageId);
                linkEntity.setIdLink(linkId);
                linksRepository.save(linkEntity);
            }
        }
    }
}
