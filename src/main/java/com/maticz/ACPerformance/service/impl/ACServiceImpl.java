package com.maticz.ACPerformance.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maticz.ACPerformance.model.Campaign;
import com.maticz.ACPerformance.model.Emails;
import com.maticz.ACPerformance.repository.CampaignRepository;
import com.maticz.ACPerformance.repository.EmailsRepository;
import com.maticz.ACPerformance.service.ACService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ACServiceImpl implements ACService {

    @Autowired
    CampaignRepository campaignRepository;

    @Autowired
    EmailsRepository emailsRepository;


    Logger logger = LoggerFactory.getLogger(ACService.class);
    @Value("${api.token}")
    private String apiToken;


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter formatterDDmmYY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    DateTimeFormatter formatterEmail = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    DateTimeFormatter formatterDB = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");




    @Override
    public String getACData() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://woop.activehosted.com/api/3/campaigns/?campaignID=";


        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Token", apiToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        String result = response.getBody();


        logger.info(result);
        return result;
    }


    @Override
    public JsonNode getCampaignID(String id, String pageNumber) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://woop.api-us1.com/admin/api.php?api_action=campaign_list&sort_direction=ASC&api_output=json&filters[id_greater]=" + id + "&page=" + pageNumber;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Token", apiToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);

        String body = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(body);
    }

    @Override
    public void saveCampaignDataToDB(String idGreaterThan) throws JsonProcessingException {

        for (int i = 1; i < 100; i++) {

            JsonNode node = getCampaignID(idGreaterThan, String.valueOf(i));

            if (Integer.parseInt(node.path("result_code").asText()) == 0) {
                break;
            } else {

                for (JsonNode data : node) {
                    Integer sendAmount = 0;
                    try {
                        sendAmount = Integer.valueOf(data.path("send_amt").asText());
                    } catch (NumberFormatException ignored) {
                    }
                    if (sendAmount > 0) {
                        Campaign cmpg = getCampaign(data, sendAmount);
                        if(campaignRepository.findByIdCampaign(cmpg.getIdCampaign()).isEmpty()){
                            campaignRepository.save(cmpg);
                        } else {
                            cmpg.setIgnoreMultipleEmails(campaignRepository.ignoreListValue(cmpg.getIdCampaign()));
                        }
                    } else {

                    }
                }
            }
        }
    }

    private Campaign getCampaign(JsonNode data, Integer sendAmount) throws JsonProcessingException {
        Integer idCampaign = 0;
        try {
            idCampaign = Integer.valueOf(data.path("id").asText());
        } catch (NumberFormatException e) {
            logger.info(data.path("id").asText());
            logger.info(data.asText());
        }
        String name = data.path("name").asText();
        Integer totalAmount = Integer.valueOf(data.path("total_amt").asText());
        Integer opens = Integer.valueOf(data.path("opens").asText());
        Integer uniqueOpens = Integer.valueOf(data.path("uniqueopens").asText());
        Integer linkClicks = Integer.valueOf(data.path("linkclicks").asText());
        Integer uniqueLinkClicks = Integer.valueOf(data.path("uniquelinkclicks").asText());
        Integer hardBounces = Integer.valueOf(data.path("hardbounces").asText());
        Integer softBounces = Integer.valueOf(data.path("softbounces").asText());
        Integer forwards = Integer.valueOf(data.path("forwards").asText());
        Integer unsubscribes = Integer.valueOf(data.path("unsubscribes").asText());
        Integer unsubreasons = Integer.valueOf(data.path("unsubreasons").asText());
        Integer verifiedOpens = Integer.valueOf(data.path("verified_opens").asText());
        Integer verifiedUniqueOpens = Integer.valueOf(data.path("verified_unique_opens").asText());
        LocalDate created = LocalDate.parse(data.path("created_timestamp").asText(), formatter);
        LocalDate updated = LocalDate.parse(data.path("updated_timestamp").asText(), formatter);
        LocalDateTime lastSendDate = LocalDateTime.parse(data.path("ldate").asText(), formatter);
        Integer subscriberClicks = Integer.valueOf(data.path("subscriberclicks").asText());
        String automationName = data.path("seriesname").asText();

        Integer idMessage = getIdMessageForCampaignInDB(String.valueOf(idCampaign));

        Campaign cmpg = new Campaign();
        cmpg.setIdCampaign(idCampaign);
        cmpg.setName(name);
        cmpg.setSendAmount(sendAmount);
        cmpg.setTotalSendAmount(totalAmount);
        cmpg.setOpens(opens);
        cmpg.setUniqueOpens(uniqueOpens);
        cmpg.setLinkClicks(linkClicks);
        cmpg.setUniqueClicks(uniqueLinkClicks);
        cmpg.setHardBounces(hardBounces);
        cmpg.setSoftBounces(softBounces);
        cmpg.setForwards(forwards);
        cmpg.setUnsubscribes(unsubscribes);
        cmpg.setUnsubreasons(unsubreasons);
        cmpg.setVerifiedOpens(verifiedOpens);
        cmpg.setVerifiedUniqueOpens(verifiedUniqueOpens);
        cmpg.setCreateDate(created);
        cmpg.setUpdateDate(updated);
        cmpg.setIdMessage(idMessage);
        cmpg.setLastSendDate(lastSendDate);
        cmpg.setSubscriberClicks(subscriberClicks);
        if (!automationName.isEmpty()) {
            cmpg.setAutomation(automationName);
        } else {
            cmpg.setAutomation(null);
        }
        return cmpg;
    }


    @Override
    public JsonNode getClientsByCampaign(String idCampaign, String pageNumber) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://woop.api-us1.com/admin/api.php?api_action=campaign_report_open_list&api_output=json&campaignid=" + idCampaign + "&page=" + pageNumber;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Token", apiToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);

        String body = response.getBody();
        logger.info(body);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(body);

        return rootNode;
    }


    @Override
    public HashMap<Integer, Integer> getDataForClientsFromCampaignsSentInPastWeek() throws JsonProcessingException {

        List<Integer> list = campaignRepository.listOfCampaignsSentInLastWeekWithoutSome();
        HashMap<Integer, Integer> hashMapOfContactAndCampaignAdded = new HashMap<>();


        for (int id : list) {
            int page = emailsRepository.getPageNumberForCampaignByIdTimesOpenedGreaterThen0(id);
            Integer messageID = null;
            try {
                messageID = campaignRepository.getIdMessageForCampaign(id);

            } catch (NumberFormatException ignored) {

            }
            for (int i = page; i < 1000; i++) {
                JsonNode node = getClientsByCampaign(String.valueOf(id), String.valueOf(i));

                if (node.path("result_code").asText() == "0") {
                    break;
                } else {

                    for (JsonNode data : node) {
                        try {
                            Integer idSubscriber = Integer.valueOf(data.path("subscriberid").asText());
                            String email = data.path("email").asText();
                            Integer times = Integer.valueOf(data.path("times").asText());
                            LocalDateTime timestamp = LocalDateTime.parse(data.path("tstamp").asText(), formatter);

                            Emails emails = new Emails();
                            emails.setEmail(email);
                            emails.setIdSubscriber(idSubscriber);
                            emails.setIdCampaign(id);
                            emails.setOpened(timestamp);
                            emails.setTimesOpened(times);
                            emails.setIdMessage(messageID);
                            emails.setEmailSent(timestamp);
                            emails.setPage(i);

                            if (emailsRepository.findByIdSubscriberAndIdCampaign(idSubscriber, id).isPresent()) {
                                emailsRepository.updateValuesByIdSubscriberAndIdCampaign(times, timestamp, idSubscriber, id, i);
                            } else {
                                emailsRepository.save(emails);
                                if (hashMapOfContactAndCampaignAdded.containsKey(id)) {

                                } else {
                                    hashMapOfContactAndCampaignAdded.put(idSubscriber, id);
                                }
                            }
                        } catch (NumberFormatException | NullPointerException ignored) {
                        }
                    }
                }
            }
        }
        return hashMapOfContactAndCampaignAdded;
    }

    @Override
    public JsonNode getCampaingDataForLastWeek(String pageNumber) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://woop.api-us1.com/admin/api.php?api_action=campaign_list&sort_direction=ASC&api_output=json&filters[ldate_since_datetime]=" + LocalDate.now().minusDays(7) + "&page=" + pageNumber;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Token", apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        String body = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(body);
    }


    @Override
    public List<Campaign> updateCampaignDataForLastWeek() throws JsonProcessingException {

        List<Campaign> savedData = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            JsonNode node = getCampaingDataForLastWeek(String.valueOf(i));

            for (JsonNode data : node) {
                if (Integer.parseInt(node.path("result_code").asText()) == 0) {
                    break;
                } else {
                    Integer sendAmount = 0;
                    try {
                        sendAmount = Integer.valueOf(data.path("send_amt").asText());
                    } catch (NumberFormatException e) {
                        break;
                    }
                    Campaign cmpg = getCampaign(data, sendAmount);
                    if(campaignRepository.findByIdCampaign(cmpg.getIdCampaign()).isEmpty()){
                        campaignRepository.save(cmpg);
                    } else {
                        cmpg.setIgnoreMultipleEmails(campaignRepository.ignoreListValue(cmpg.getIdCampaign()));
                        cmpg.setImportToFactEmails(campaignRepository.importToFactValue(cmpg.getIdCampaign()));
                    }
                    savedData.add(cmpg);

                }
            }
        }
        return savedData;
    }


    @Override
    public Integer getIdMessageForCampaignInDB(String id) throws JsonProcessingException {


        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://woop.activehosted.com/api/3/campaigns/" + String.valueOf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Token", apiToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());

        logger.info(String.valueOf(rootNode));
        Integer idMessage = null;
        for (JsonNode data : rootNode) {

            idMessage = Integer.valueOf(data.path("message_id").asText());

        }
        return idMessage;
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
    public HashMap<Integer, Integer> saveContactsThatHaventOpenedEmail() throws JsonProcessingException {

        List<Object[]> idsLast7Days = campaignRepository.listOfIdsAndIdMessagesForCampaingsSentInLast7Days();
        HashMap<Integer, Integer> hashMapOfIdSubscribersAdded = new HashMap<>();


        for (Object row[] : idsLast7Days) {
            Integer pageNum = emailsRepository.getPageNumberForCampaignByIdTimesOpenedEquals0(Integer.parseInt(row[0].toString()));
            for (int i = pageNum; i < 1000; i++) {
                JsonNode node = getDataForContactsThatHaveNotOpenedEmail(row[0].toString(), row[1].toString(), String.valueOf(i));

                if (Objects.equals(node.path("result_code").asText(), "0")) {
                    break;
                } else {
                    for (JsonNode data : node) {
                        try {
                            Integer idSubscriber = Integer.valueOf(data.path("subscriberid").asText());
                            String email = data.path("email").asText();
                            Integer times = Integer.valueOf(data.path("times").asText());
                            Integer idCampaign = Integer.valueOf(row[0].toString());

                            Emails emails = new Emails();
                            emails.setEmail(email);
                            emails.setIdSubscriber(idSubscriber);
                            emails.setIdCampaign(idCampaign);
                            emails.setTimesOpened(times);
                            emails.setIdMessage(Integer.valueOf(row[1].toString()));
                            emails.setPage(i);
                            logger.info(emails.toString());
                            logger.info(String.valueOf(i));

                            if (emailsRepository.findByIdSubscriberAndIdCampaign(idSubscriber, Integer.valueOf(row[0].toString())).isPresent()) {
                            } else {
                                emailsRepository.save(emails);
                                if (hashMapOfIdSubscribersAdded.containsKey(idCampaign)) {

                                } else {
                                    hashMapOfIdSubscribersAdded.put(idSubscriber, idCampaign);
                                }
                            }

                        } catch (NumberFormatException | NullPointerException ignored) {
                        }
                    }
                }
            }
        }
        return hashMapOfIdSubscribersAdded;
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
    public JsonNode getContactActivitiesAfterDate(String idSubscriber, String date) throws JsonProcessingException {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY = TimeUnit.SECONDS.toMillis(1);

        int retryCount = 0;
        boolean success = false;

        ResponseEntity<String> response = null;
        ObjectMapper objectMapper = null;
        while (!success && retryCount < MAX_RETRIES) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String baseUrl = "https://woop.api-us1.com/api/3/activities?contact=" + idSubscriber + "&after=" + date + "&orders[tstamp]=asc&api_output=json&limit=1000";
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
    public void saveContactEmailActivityAfterDate() throws JsonProcessingException {

        List<Object[]> idSubscribersWithoutEmailSent = emailsRepository.getIdSubscriberWithountEmailSentWithConcatenatedIdCampaign();

        for (Object[] row : idSubscribersWithoutEmailSent) {
            String idSubscriber = row[1].toString();
            String date = String.valueOf(LocalDate.parse(row[0].toString(), formatterDB));
            Integer count = Integer.parseInt(row[2].toString());
            List<String> idCampaignList = new ArrayList<>(List.of(row[3].toString().split("\\|")));

            AtomicReference<LocalDate> lastDate = new AtomicReference<>(LocalDate.parse(date));

            while (count > 0 && lastDate.get().isBefore(LocalDate.now())) {
                JsonNode activityNode = getContactActivitiesAfterDate(idSubscriber, lastDate.get().toString());
                logger.info("idSubscriber: " + idSubscriber + " date: " + lastDate.get().toString() + " campaignList : " + idCampaignList);
                if (!activityNode.path("logs").isEmpty()) {
                    Integer nodeSize = activityNode.path("logs").size();
                    for (JsonNode logsNode : activityNode.path("logs")) {
                        String idCampaign = logsNode.path("campaignid").asText();
                        ZonedDateTime originalTime = ZonedDateTime.parse(logsNode.path("tstamp").asText());
                        LocalDateTime targetDateTime = originalTime.withZoneSameInstant(ZoneId.of("UTC+01:00")).toLocalDateTime();
                        lastDate.set(LocalDate.of(targetDateTime.getYear(), targetDateTime.getMonthValue(), targetDateTime.getDayOfMonth()));

                        if (idCampaignList.contains(idCampaign)) {
                            emailsRepository.updateClientsEmailSendDateWhereEmailSentIsNull(targetDateTime, Integer.valueOf(idCampaign), Integer.valueOf(idSubscriber));
                            logger.info("in list: " + idCampaign + " timestamp: " + targetDateTime.toString());
                            count -= 1;
                            idCampaignList.remove(idCampaign);
                        } else {
                            nodeSize -= 1;
                            if (nodeSize == 0) {
                                lastDate.set(lastDate.get().plusDays(3));
                            }
                        }
                    }
                } else if (!activityNode.path("message").isEmpty()) {
                    count = 0;
                } else if (activityNode.path("meta").path("total").asInt() != 0) {
                    lastDate.set(lastDate.get().plusDays(3));
                } else {
                    count = 0;
                }
            }
        }
    }

    @Override
    public void saveContactDataForOpenedAndUnopenedEmails2() throws JsonProcessingException {

        HashMap<Integer, LocalDateTime> campaignAndTimestamp = getDataForClientsThatOpenedCampaignsAndAddTimestampToMap();
        // unopen clients...
        List<Object[]> idsLast7Days = campaignRepository.listOfIdsAndIdMessagesForCampaingsSentInLast7Days();

        idsLast7Days.forEach(row -> {
            if (campaignAndTimestamp.containsKey(Integer.parseInt(row[0].toString()))) {
                Integer pageNum = emailsRepository.getPageNumberForCampaignByIdTimesOpenedEquals0(Integer.parseInt(row[0].toString()));
                LocalDateTime sentTimestamp = campaignAndTimestamp.get(Integer.parseInt(row[0].toString()));

                for (int i = pageNum; i < 1000; i++) {
                    JsonNode node = null;
                    try {
                        node = getDataForContactsThatHaveNotOpenedEmail(row[0].toString(), row[1].toString(), String.valueOf(i));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    if (Objects.equals(node.path("result_code").asText(), "0")) {
                        break;
                    } else {
                        for (JsonNode data : node) {
                            try {
                                if (Objects.equals(node.path("result_code").asText(), "0")) {
                                    break;
                                } else {
                                    Integer idSubscriber = Integer.valueOf(data.path("subscriberid").asText());
                                    String email = data.path("email").asText();
                                    Integer times = Integer.valueOf(data.path("times").asText());
                                    Integer idCampaign = Integer.valueOf(row[0].toString());

                                    Emails emails = new Emails();
                                    emails.setEmail(email);
                                    emails.setIdSubscriber(idSubscriber);
                                    emails.setIdCampaign(idCampaign);
                                    emails.setTimesOpened(times);
                                    emails.setIdMessage(Integer.valueOf(row[1].toString()));
                                    emails.setPage(i);
                                    emails.setEmailSent(sentTimestamp);
                                    emails.setImportTimestamp(LocalDateTime.now());
                                    logger.info(emails.toString()); /// poprav če dobi isto kampanjo drugo leto !
                                    if (emailsRepository.findByIdSubscriberAndIdCampaignAndPage(idSubscriber, idCampaign, i).isEmpty()) {
                                        emailsRepository.save(emails);
                                    }
                                }

                            } catch (NumberFormatException | NullPointerException ignored) {
                            }
                        }
                    }
                }
            } else {
                Integer pageNum = emailsRepository.getPageNumberForCampaignByIdTimesOpenedEquals0(Integer.parseInt(row[0].toString()));
                for (Integer j = pageNum; j < 100; j++) {
                    try {
                        JsonNode node = getDataForContactsThatHaveNotOpenedEmail(row[0].toString(), row[1].toString(), String.valueOf(j));
                        Boolean skip = false;
                        if (!Objects.equals(node.path("result_code").asText(), "0")) {
                            Integer idSubscriber = null;
                            String email = null;
                            Integer times = null;
                            try {
                                idSubscriber = Integer.valueOf(node.path("subscriberid").asText());
                                email = node.path("email").asText();
                                times = Integer.valueOf(node.path("times").asText());
                            } catch (NumberFormatException | NullPointerException e) {
                                skip = true;
                            }
                            if (skip == false) {
                                Integer idCampaign = Integer.valueOf(row[0].toString());

                                Emails emails = new Emails();
                                emails.setEmail(email);
                                emails.setIdSubscriber(idSubscriber);
                                emails.setIdCampaign(idCampaign);
                                emails.setTimesOpened(times);
                                emails.setIdMessage(Integer.valueOf(row[1].toString()));
                                emails.setPage(j);


                                if (campaignAndTimestamp.containsKey(idCampaign)) {
                                    emails.setEmailSent(campaignAndTimestamp.get(idCampaign));
                                } else {
                                    JsonNode contactActivityNode = null;
                                    try {
                                        contactActivityNode = getContactActivities(String.valueOf(idSubscriber));
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if (!contactActivityNode.path("message").asText().contains("No result")) {
                                        contactActivityNode.path("logs").forEach(object -> {
                                            Integer idCampaignActivity = Integer.valueOf(object.path("campaignid").asText());

                                            if (idCampaign == idCampaignActivity) {
                                                ZonedDateTime originalTime = ZonedDateTime.parse(object.path("tstamp").asText());
                                                ZonedDateTime targetTime = originalTime.withZoneSameInstant(ZoneId.of("UTC+01:00"));
                                                campaignAndTimestamp.put(idCampaignActivity, targetTime.toLocalDateTime());
                                                emails.setEmailSent(targetTime.toLocalDateTime());
                                            }
                                        });
                                    }
                                    emails.setImportTimestamp(LocalDateTime.now());
                                    logger.info(emails.toString());

                                }
                                if (emailsRepository.findByIdSubscriberAndIdCampaignAndPage(idSubscriber, idCampaign, j).isEmpty()) {
                                    emailsRepository.save(emails);
                                }
                            } else {
                                break;
                            }
                        }
                    } catch (JsonProcessingException e) {
                        System.err.println("najbrz prazn activity" + e.toString());
                    }


                    campaignAndTimestamp.forEach((key, value) -> {
                        emailsRepository.updateEveryClientsEmailSendDateBasedOnIdCampaignAndSentDateNullAndCurrentDate(value, key);
                    });

                }

            }
        });
    }

    @Override
    public HashMap<Integer, LocalDateTime> getDataForClientsThatOpenedCampaignsAndAddTimestampToMap() throws JsonProcessingException {

        List<Integer> list = campaignRepository.listOfCampaignsSentInLastWeekWithoutSome();
        HashMap<Integer, LocalDateTime> mapIdCampaignTimestamp = new HashMap<>();

        for (int id : list) {
            int page = emailsRepository.getPageNumberForCampaignByIdTimesOpenedGreaterThen0(id);
            Integer messageID = null;
            try {
                messageID = campaignRepository.getIdMessageForCampaign(id);

            } catch (NumberFormatException ignored) {
            }
            for (int i = page; i < 1000; i++) {
                JsonNode node = getClientsByCampaign(String.valueOf(id), String.valueOf(i));
                logger.info(" JsonNode: {}", node);

                if (Objects.equals(node.path("result_code").asText(), "0")) {
                    break;
                } else {
                    for (JsonNode data : node) {
                        try {

                            Integer idSubscriber = data.path("subscriberid").asInt();
                            String email = data.path("email").asText();
                            Integer times = data.path("times").asInt();
                            LocalDateTime timestamp = LocalDateTime.parse(data.path("tstamp").asText(), formatter);

                            Emails emails = new Emails();
                            emails.setEmail(email);
                            emails.setIdSubscriber(idSubscriber);
                            emails.setIdCampaign(id);
                            emails.setOpened(timestamp);
                            emails.setTimesOpened(times);
                            emails.setIdMessage(messageID);
                            emails.setEmailSent(timestamp);
                            emails.setImportTimestamp(LocalDateTime.now());
                            emails.setPage(i);

                            if (emailsRepository.findByIdSubscriberAndIdCampaignAndOpened(idSubscriber, id, LocalDate.from(emails.getOpened())).isPresent()) {
                                emailsRepository.updateValuesByIdSubscriberAndIdCampaign(times, timestamp, idSubscriber, id, page);
                            } else {
                                emailsRepository.save(emails);
                                if (mapIdCampaignTimestamp.containsKey(id)) {

                                } else {
                                    mapIdCampaignTimestamp.put(id, timestamp);
                                }
                            }
                        } catch (NumberFormatException | NullPointerException e) {
                            logger.info("ERROR: " + e.getMessage()  +  "\n" + Arrays.toString(e.getStackTrace()) );
                        }
                    }
                }
            }
        }
        return mapIdCampaignTimestamp;
    }

    @Override
    public HashMap<Integer, LocalDateTime> getDataForClientsThatOpenedCampaignsAndAddTimestampToMap2() throws JsonProcessingException {
        List<Integer> list = campaignRepository.listOfCampaignsSentInLastWeekWithoutSome();
        HashMap<Integer, LocalDateTime> mapIdCampaignTimestamp = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int id : list) {
            int page = emailsRepository.getPageNumberForCampaignByIdTimesOpenedGreaterThen0(id);
            Integer messageID = null;
            try {
                messageID = campaignRepository.getIdMessageForCampaign(id);
            } catch (NumberFormatException e) {
                logger.warn("Failed to get message ID for campaign {}: {}", id, e.getMessage());
            }

            for (int i = page; i < 1000; i++) {
                JsonNode node = getClientsByCampaign(String.valueOf(id), String.valueOf(i));
                logger.info("Processing JsonNode for campaign {} page {}: {}", id, i, node);

                String resultCode = node.path("result_code").asText();
                if ("0".equals(resultCode)) {
                    logger.info("No data to process for campaign {} page {} (result_code: 0)", id, i);
                    break;
                }
                for (JsonNode data : node) {
                    if (!data.isObject()) {
                        continue;
                    }

                    try {
                        Integer idSubscriber = data.path("subscriberid").asInt();
                        String email = data.path("email").asText();
                        Integer times = data.path("times").asInt();
                        String tstampStr = data.path("tstamp").asText();

                        if (tstampStr == null || tstampStr.isEmpty()) {
                            logger.warn("Empty timestamp for subscriber {} in campaign {}", idSubscriber, id);
                            continue;
                        }

                        LocalDateTime timestamp = LocalDateTime.parse(tstampStr, formatter);

                        Emails emails = new Emails();
                        emails.setEmail(email);
                        emails.setIdSubscriber(idSubscriber);
                        emails.setIdCampaign(id);
                        emails.setOpened(timestamp);
                        emails.setTimesOpened(times);
                        emails.setIdMessage(messageID);
                        emails.setEmailSent(timestamp);
                        emails.setImportTimestamp(LocalDateTime.now());
                        emails.setPage(i);

                        if (emailsRepository.findByIdSubscriberAndIdCampaignAndOpened(idSubscriber, id, LocalDate.from(emails.getOpened())).isPresent()) {
                            emailsRepository.updateValuesByIdSubscriberAndIdCampaign(times, timestamp, idSubscriber, id, page);
                        } else {
                            emailsRepository.save(emails);
                            if (!mapIdCampaignTimestamp.containsKey(id)) {
                                mapIdCampaignTimestamp.put(id, timestamp);
                            }
                        }
                    } catch (DateTimeParseException e) {
                        logger.error("Error parsing timestamp for subscriber in campaign {} page {}: {}", id, i, e.getMessage());
                    } catch (Exception e) {
                        logger.error("Error processing subscriber data for campaign {} page {}: {}", id, i, e.getMessage());
                    }
                }
            }
        }

        return mapIdCampaignTimestamp;
    }

    @Override
    public void saveContactsWhereDataIsMissingForPhotos() throws JsonProcessingException {
        List<Object[]> minPageNumbAndCampaign = emailsRepository.getMinPageWhereCountLessThen20ForBdayPhotos();

        minPageNumbAndCampaign.forEach(row -> {
            String pageNumb = row[0].toString();
            String idCampaign = row[1].toString();


            JsonNode node = null;
            try {
                node = getClientsByCampaign(idCampaign, pageNumb);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (Objects.equals(node.path("result_code").asText(), "0")) {

            } else {
                for (JsonNode data : node) {
                    try {
                        logger.info(node.asText());
                        Integer idSubscriber = Integer.valueOf(data.path("subscriberid").asText());
                        String email = data.path("email").asText();
                        Integer times = Integer.valueOf(data.path("times").asText());

                        Emails emails = new Emails();
                        emails.setEmail(email);
                        emails.setIdSubscriber(idSubscriber);
                        emails.setIdCampaign(Integer.valueOf(idCampaign));
                        emails.setTimesOpened(times);
                        emails.setPage(Integer.parseInt(pageNumb));
                        logger.info(emails.toString());

                        if (emailsRepository.findByIdSubscriberAndIdCampaign(idSubscriber, Integer.parseInt(idCampaign)).isPresent()) {

                        } else {
                            emailsRepository.save(emails);
                        }
                    } catch (NumberFormatException | NullPointerException ignored) {
                    }
                }
            }

        });
    }


}










