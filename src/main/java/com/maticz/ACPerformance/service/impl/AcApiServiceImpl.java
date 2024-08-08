package com.maticz.ACPerformance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maticz.ACPerformance.service.AcApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class AcApiServiceImpl implements AcApiService {

    @Value("${api.token}")
    private String apiToken;

    @Override
    public JsonNode getUnsubscriptionsByCampaign(String idCampaign, String page) throws JsonProcessingException {

        final int MAX_RETRIES = 3;
        final long RETRY_DELAY = TimeUnit.SECONDS.toMillis(1);

        int retryCount = 0;
        boolean success = false;

        ResponseEntity<String> response = null;
        ObjectMapper objectMapper = null;
        while (!success && retryCount < MAX_RETRIES) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String baseUrl = "https://woop.api-us1.com/admin/api.php?api_action=campaign_report_unsubscription_list&api_output=json&campaignid=" + idCampaign + "&page=" + page + "&api_output=json";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Api-Token", apiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
                success = true;
                objectMapper = new ObjectMapper();

            } catch (HttpServerErrorException e) {
                System.err.println(" failed: " + e.getMessage());

                retryCount++;

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return objectMapper.readTree(response.getBody());
    }


    @Override
    public JsonNode getContactActivities(String idSubscriber) throws JsonProcessingException {

        final int MAX_RETRIES = 3;
        final long RETRY_DELAY = TimeUnit.SECONDS.toMillis(1);

        int retryCount = 0;
        boolean success = false;

        ResponseEntity<String> response = null;
        ObjectMapper objectMapper = null;
        while (!success && retryCount < MAX_RETRIES) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String baseUrl = "https://woop.api-us1.com/api/3/activities?contact=" + idSubscriber + "&orders[tstamp]=desc&limit=1000&api_output=json";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Api-Token", apiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
                success = true;
                objectMapper = new ObjectMapper();

            } catch (HttpServerErrorException e) {
                System.err.println(" failed: " + e.getMessage());

                retryCount++;

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return objectMapper.readTree(response.getBody());
    }


    @Override
    public JsonNode getDataForContactsThatHaveNotOpenedEmail(String idCampaign, String idMessage, String pageNumber) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://woop.api-us1.com/admin/api.php?api_action=campaign_report_unopen_list&api_output=json&campaignid=" + idCampaign + "&messageid=" + idMessage + "&page=" + pageNumber;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Token", apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(response.getBody());
    }

    @Override
    public JsonNode getLinkData(String idCampaign) throws JsonProcessingException {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY = TimeUnit.SECONDS.toMillis(1);

        int retryCount = 0;
        boolean success = false;

        ResponseEntity<String> response = null;
        ObjectMapper objectMapper = null;
        while (!success && retryCount < MAX_RETRIES) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String baseUrl = "https://woop.api-us1.com/api/3/campaigns/" + idCampaign + "/links";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Api-Token", apiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
                success = true;
                objectMapper = new ObjectMapper();

            } catch (HttpServerErrorException e) {
                System.err.println(" failed: " + e.getMessage());

                retryCount++;

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return objectMapper.readTree(response.getBody());
    }

    @Override
    public JsonNode getMPPLinkData(String idLink) throws JsonProcessingException {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY = TimeUnit.SECONDS.toMillis(1);

        int retryCount = 0;
        boolean success = false;

        ResponseEntity<String> response = null;
        ObjectMapper objectMapper = null;
        while (!success && retryCount < MAX_RETRIES) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String baseUrl = "https://woop.api-us1.com/api/3/links/" + idLink + "/mppLinkData";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Api-Token", apiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
                success = true;
                objectMapper = new ObjectMapper();

            } catch (HttpServerErrorException e) {
                System.err.println(" failed: " + e.getMessage());

                retryCount++;

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return objectMapper.readTree(response.getBody());
    }
}