package com.maticz.ACPerformance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.maticz.ACPerformance.model.Emails;
import com.maticz.ACPerformance.repository.CampaignRepository;
import com.maticz.ACPerformance.repository.EmailsRepository;
import com.maticz.ACPerformance.repository.EmailsRepositoryNew;
import com.maticz.ACPerformance.service.ACServiceNew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class ACServiceNewImpl implements ACServiceNew {

    @Autowired
    EmailsRepositoryNew emailsRepositoryNew;

    @Autowired
    EmailsRepository emailsRepository;

    @Autowired
    ACServiceImpl acServiceImpl;

    @Autowired
    CampaignRepository campaignRepository;


    DateTimeFormatter formatterISOOffset = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    Logger logger = LoggerFactory.getLogger(ACServiceNewImpl.class);

    @Override
    public void getDataFromACAndSaveToDB() throws JsonProcessingException {

        HashMap<Integer, LocalDateTime> idCampaignAndTimestamp =  acServiceImpl.getDataForClientsThatOpenedCampaignsAndAddTimestampToMap2();
        List<Integer> campaignList = campaignRepository.listOfCampaignsSentInLastWeekWithoutSome();
        for (Integer idCampaign : campaignList) {
            String idMessage = String.valueOf(campaignRepository.getIdMessageForCampaign(idCampaign));

            Integer page = emailsRepository.getPageNumberForCampaignByIdTimesOpenedEquals0(idCampaign);
            for(Integer i = page ; i < 1000 ; i++) {
                JsonNode unOpenedData = acServiceImpl.getDataForContactsThatHaveNotOpenedEmail(String.valueOf(idCampaign), idMessage, String.valueOf(page));
                logger.info("Processing JsonNode for campaign {} page {}: {}", idCampaign, i, unOpenedData);

                if(unOpenedData.path("result_code").asInt() == 1) {
                    for (JsonNode data : unOpenedData) {
                        Integer idSubscriber = data.path("subscriberid").asInt();
                        String email = data.path("email").asText();
                        Integer timesOpened = data.path("times").asInt();
                        Emails emails = setEmailsEntityWithoutOpenedTimestamp(idCampaign, email, idSubscriber, timesOpened, idMessage, i);

                        if (idCampaignAndTimestamp.containsKey(idCampaign)) {
                            emails.setOpened(idCampaignAndTimestamp.get(idCampaign));
                            if (emailsRepository.findByIdSubscriberAndIdCampaignAndPage(idSubscriber, idCampaign, i).isEmpty()) {
                                emailsRepository.save(emails);
                            }
                        } else {
                            emails.setOpened(getTimestampForContactThatIsNotInMap(String.valueOf(idSubscriber), idCampaign));
                            if (emailsRepository.findByIdSubscriberAndIdCampaignAndPage(idSubscriber, idCampaign, i).isEmpty()) {
                                emailsRepository.save(emails);
                            }
                        }

                    }
                }else {
                    break;
                }

            }
        }
    }

    @Override
    public LocalDateTime getTimestampForContactThatIsNotInMap(String idSubscriber,  Integer idCampaign) throws JsonProcessingException {
        String dateFrom = emailsRepository.dateToSearchByForUnopenedClients(idCampaign);
        Boolean timestampFound = false;
        Boolean nodeResultEmpty = false;
        LocalDateTime emailSentTimestamp = null;
        LocalDateTime timestamp = LocalDateTime.now().minusDays(10);
        Boolean sameTimestamp = false;

            while(!timestampFound && !nodeResultEmpty && timestamp.isBefore(LocalDateTime.now()) && !sameTimestamp){
                JsonNode node = acServiceImpl.getContactActivitiesAfterDate(idSubscriber,dateFrom);

                    for(JsonNode data : node){
                        if(node.path("meta").path("total").asInt() > 0) {
                                if(!data.path("logs").isEmpty()){
                                    for(JsonNode logs : data.path("logs")){
                                        Integer logsIdCampaign = logs.path("campaignid").asInt();
                                        OffsetDateTime originalTimestamp = OffsetDateTime.parse(logs.path("tstamp").asText(), formatterISOOffset);
                                            if(timestamp == originalTimestamp.atZoneSameInstant(ZoneId.of("Europe/Ljubljana")).toLocalDateTime() ){
                                                sameTimestamp = true;
                                                dateFrom = String.valueOf(LocalDate.from(timestamp).plusDays(5));
                                                break;
                                            }else {
                                                timestamp = originalTimestamp.atZoneSameInstant(ZoneId.of("Europe/Ljubljana")).toLocalDateTime();
                                                dateFrom = String.valueOf(LocalDate.from(timestamp));
                                            }

                                        if(logsIdCampaign == idCampaign ){
                                            emailSentTimestamp = timestamp;
                                            timestampFound = true;
                                        }
                                    }
                                }else {
                                    timestamp = timestamp.plusDays(5);
                                }

                        }else {
                            nodeResultEmpty = true;
                            break;
                        }
                    }
            }
        return emailSentTimestamp;
    }

    private static Emails setEmailsEntityWithoutOpenedTimestamp(Integer idCampaign, String email, Integer idSubscriber, Integer timesOpened, String idMessage, Integer i) {
        Emails emails = new Emails();
            emails.setEmail(email);
            emails.setIdSubscriber(idSubscriber);
            emails.setTimesOpened(timesOpened);
            emails.setImportTimestamp(LocalDateTime.now());
            emails.setIdCampaign(idCampaign);
            emails.setIdMessage(Integer.valueOf(idMessage));
            emails.setPage(i);
        return emails;
    }


}
