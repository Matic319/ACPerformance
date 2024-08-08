package com.maticz.ACPerformance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.maticz.ACPerformance.model.Emails;
import com.maticz.ACPerformance.repository.CampaignRepository;
import com.maticz.ACPerformance.repository.EmailsRepository;
import com.maticz.ACPerformance.repository.EmailsRepositoryNew;
import com.maticz.ACPerformance.service.ACServiceNew;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Autowired
            AcApiServiceImpl acApiService;

    DateTimeFormatter formatterISOOffset = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    Logger logger = LoggerFactory.getLogger(ACServiceNewImpl.class);

    @Override
    public void getDataFromACAndSaveToDB() throws JsonProcessingException {

        HashMap<Integer, LocalDateTime> idCampaignAndTimestamp = acServiceImpl.getDataForClientsThatOpenedCampaignsAndAddTimestampToMap2();
        List<Integer> campaignList = campaignRepository.listOfCampaignsSentInLastWeekWithoutSome();
        for (Integer idCampaign : campaignList) {
            String idMessage = String.valueOf(campaignRepository.getIdMessageForCampaign(idCampaign));

            Integer page = emailsRepository.getPageNumberForCampaignByIdTimesOpenedEquals0(idCampaign);
            for (Integer i = page; i < 1000; i++) {
                JsonNode unOpenedData = acApiService.getDataForContactsThatHaveNotOpenedEmail(String.valueOf(idCampaign), idMessage, String.valueOf(i));
                logger.info("JsonNode for campaign {} page {}: {}", idCampaign, i, unOpenedData);

                if (unOpenedData.path("result_code").asInt() == 1) {
                    for (JsonNode data : unOpenedData) {
                        Integer idSubscriber = data.path("subscriberid").asInt();
                        String email = data.path("email").asText();
                        Integer timesOpened = data.path("times").asInt();
                        Emails emails = setEmailsEntityWithoutOpenedTimestamp(idCampaign, email, idSubscriber, timesOpened, idMessage, i);
                        if (idSubscriber != 0) {
                            if (idCampaignAndTimestamp.get(idCampaign) != null) {
                                emails.setEmailSent(idCampaignAndTimestamp.get(idCampaign));
                                logger.info("find idSubscriber {} , idCampaign {} , page {}", idSubscriber, idCampaign, i);
                                if (emailsRepository.findByIdSubscriberAndIdCampaignAndPageAndImportTimestamp(idSubscriber, idCampaign, i).isEmpty()) {
                                    if (idSubscriber != 0) {
                                        emailsRepository.save(emails);
                                    }

                                }
                            } else {
                                logger.info("find idSubscriber {} , idCampaign {} , page {}", idSubscriber, idCampaign, i);

                                if (emailsRepository.findByIdSubscriberAndIdCampaignAndPageAndImportTimestamp(idSubscriber, idCampaign, i).isEmpty()) {
                                    emails.setEmailSent(getTimestampForContactThatIsNotInMap(String.valueOf(idSubscriber), idCampaign));
                                    if (idSubscriber != 0) {
                                        emailsRepository.save(emails);
                                    }

                                }
                            }
                        }
                    }
                } else {
                    break;
                }

            }
        }
    }

    @Override
    public LocalDateTime getTimestampForContactThatIsNotInMap(String idSubscriber, Integer idCampaign) throws JsonProcessingException {
        String dateFrom = emailsRepository.dateToSearchByForUnopenedClients(idCampaign);
        logger.info("Searching for campaign ID: {}, subscriber ID: {}, initial dateFrom: {}", idCampaign, idSubscriber, dateFrom);

        LocalDateTime emailSentTimestamp = null;
        LocalDateTime lastProcessedTimestamp = null;

        while (LocalDate.parse(dateFrom).isBefore(LocalDate.now())) {
            JsonNode node = acServiceImpl.getContactActivitiesAfterDate(idSubscriber, dateFrom);

            if (node.has("logs")) {
                JsonNode logs = node.path("logs");
                for (JsonNode log : logs) {
                    Integer logsIdCampaign = log.path("campaignid").asInt();
                    String tstamp = log.path("tstamp").asText();
                    logger.info(" Logs Campaign ID: {}, Timestamp: {}", logsIdCampaign, tstamp);

                    OffsetDateTime originalTimestamp = OffsetDateTime.parse(tstamp, formatterISOOffset);
                    LocalDateTime convertedTimestamp = originalTimestamp.atZoneSameInstant(ZoneId.of("Europe/Ljubljana")).toLocalDateTime();

                    if (lastProcessedTimestamp == null || convertedTimestamp.isAfter(lastProcessedTimestamp)) {
                        lastProcessedTimestamp = convertedTimestamp;
                        dateFrom = convertedTimestamp.toLocalDate().toString();
                        logger.info("Updated dateFrom: {}", dateFrom);

                        if (logsIdCampaign.equals(idCampaign)) {
                            emailSentTimestamp = convertedTimestamp;
                            logger.info("campaign ID found mail sent timestamp: {}", emailSentTimestamp);
                            return emailSentTimestamp;
                        }
                    } else {
                        dateFrom = LocalDate.parse(dateFrom).plusDays(1).toString();
                    }
                }
            } else {
                logger.info("No logs found for date: {}", dateFrom);
                dateFrom = LocalDate.parse(dateFrom).plusDays(1).toString();
            }

            if (node.path("meta").path("total").asInt() == 0) {
                logger.info("No more entries found after date: {}", dateFrom);
                break;
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



    public List<String[]> readAllLines(Path filePath) throws Exception {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                return csvReader.readAll();
            }
        }
    }

    public List<String[]> readAllLinesExample() throws Exception {
        Path path = Paths.get(
                ClassLoader.getSystemResource("C:\\Users\\Matic\\Desktop\\listUnsub.csv").toURI());
        return readAllLines(path);
    }

    public List<String[]> readLineByLine(Path filePath) throws Exception {
        List<String[]> list = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    list.add(line);
                }
            }
        }
        return list;
    }
}
