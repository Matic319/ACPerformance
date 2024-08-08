package com.maticz.ACPerformance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.maticz.ACPerformance.model.Unsubscriptions;
import com.maticz.ACPerformance.repository.CampaignRepository;
import com.maticz.ACPerformance.repository.UnsubscriptionsRepository;
import com.maticz.ACPerformance.service.UnsubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;

@Service
public class UnsubscriptionsServiceImpl implements UnsubscriptionService {

    @Autowired
    UnsubscriptionsRepository unsubscriptionsRepository;

    @Autowired
    CampaignRepository campaignRepository;

    @Autowired
    AcApiServiceImpl acApiService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    ZoneId ljubljanaZone = ZoneId.of("Europe/Ljubljana");


    @Override
    public void saveUnsubscriptionsToDB() throws JsonProcessingException {

        List<Object[]> idCampaignsList = campaignRepository.listOfIdsAndIdMessagesForCampaingsSentInLast7Days();

        for (Object[] idCampaign : idCampaignsList) {
            String idCampaignString = idCampaign[0].toString();
            Integer idCampaignInt = Integer.valueOf(idCampaignString);
            Integer page = unsubscriptionsRepository.getMaxPageForUnsubscribitionByIdCampaign(Integer.valueOf(idCampaignString));

            for(Integer i = page; i < 1000 ; i++){
                JsonNode node = acApiService.getUnsubscriptionsByCampaign(idCampaignString,String.valueOf(i));

                String resultCode = node.path("result_code").asText();
                    if ("0".equals(resultCode)) {
                        break;
                    }else {
                        for(JsonNode data : node) {
                            if(data.has("subscriberid")){
                                Integer idSubscriber = data.path("subscriberid").asInt();
                                String email = data.path("email").asText();
                                System.out.println(data.toString());
                                LocalDateTime unsubTimestamp = LocalDateTime.parse(data.path("tstamp_iso").asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                        .atZone(ljubljanaZone)
                                        .withZoneSameInstant(ZoneOffset.UTC)
                                        .toLocalDateTime();

                                if(unsubscriptionsRepository.findByIdSubscriberAndIdCampaignAndPage(idSubscriber,idCampaignInt,i)
                                        .isEmpty()) {
                                    unsubscriptionsRepository.save(getUnsubsciber(idSubscriber,email,idCampaignInt,unsubTimestamp,i));

                                }
                            }

                        }
                    }

                }
            }
        }

        private Unsubscriptions getUnsubsciber (Integer idSubscriber, String email , Integer idCampaign, LocalDateTime unSubTimestamp, Integer page) {
            Unsubscriptions unsubscriptions = new Unsubscriptions();
            unsubscriptions.setIdSubscriber(idSubscriber);
            unsubscriptions.setEmail(email);
            unsubscriptions.setIdCampaign(idCampaign);
            unsubscriptions.setPage(page);
            unsubscriptions.setUnSubscriptionDate(unSubTimestamp);
            return unsubscriptions;
        }
    }


