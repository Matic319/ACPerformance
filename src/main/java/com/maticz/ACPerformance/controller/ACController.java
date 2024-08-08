package com.maticz.ACPerformance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maticz.ACPerformance.model.Campaign;
import com.maticz.ACPerformance.repository.CampaignRepository;
import com.maticz.ACPerformance.service.ACServiceNew;
import com.maticz.ACPerformance.service.impl.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/AC")
public class ACController {
    @Autowired
    ACServiceImpl acService;

     @Autowired
     ACServiceNew acServiceNew;

    @Autowired
    EmailWarningImpl emailWarning;

    @Autowired
    UnsubscriptionsServiceImpl unsubscriptionsService;

    @Autowired
    CampaignRepository campaignRepository;

    @Autowired
    LinksServiceImpl linksService;

    @GetMapping("/saveUnopened")
    ResponseEntity<String> saveUnopenedClients() throws JsonProcessingException {
        acService.saveContactsThatHaventOpenedEmail();
        return ResponseEntity.ok("ok");
    }

    @Scheduled(cron ="0 30 2 * * *" )
    @GetMapping("/saveCampaigns")
    ResponseEntity<List<Campaign>> saveCampaignsThatWereSentInLast7Days() throws JsonProcessingException {
        List<Campaign> savedData = acService.updateCampaignDataForLastWeek();
        return ResponseEntity.ok(savedData);
    }

    //@Scheduled(cron ="0 35 2 * * *" )
    @GetMapping("/saveOpened")
    ResponseEntity<String> saveClientsThatOpenedEmail() throws JsonProcessingException {
        acService.getDataForClientsFromCampaignsSentInPastWeek();
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/getAllCampaigns")
    ResponseEntity<String> getAllCampaigns() throws JsonProcessingException {
        acService.saveCampaignDataToDB("1");
        return ResponseEntity.ok("ok");
    }



    @GetMapping("/newSave")
   // @Scheduled(cron = "0 0 */2 * * *")
    ResponseEntity<String> saveOpenAndUnopen() throws JsonProcessingException {
        acService.saveContactDataForOpenedAndUnopenedEmails2();
        return ResponseEntity.ok("dela");
    }

    @Scheduled(cron = "0 0 3 * * *")
    @GetMapping("/getSentEmailDate")
    ResponseEntity<String> test2() throws JsonProcessingException {
        acService.saveContactEmailActivityAfterDate();
        return ResponseEntity.ok("ok");

    }


    @Scheduled(cron = "0 0 13 * * * ")
    @GetMapping("/sendMail")
    ResponseEntity<String> sendEmail1() throws MessagingException {
        emailWarning.sendEmailWarningWhenClientsReceiveMoreThenOneEmail();
        return ResponseEntity.ok("ok");
    }

    @Scheduled(cron = "0 0 17 * * * ")
    @GetMapping("/sendMail2")
    ResponseEntity<String> sendEmail2() throws MessagingException {

        emailWarning.sendEmailWarningWhenClientsReceiveMoreThenOneEmail();
        return ResponseEntity.ok("ok");
    }



    @Scheduled(cron = "0 0 */2 * * *")
    @GetMapping("saveAll")
    ResponseEntity<String> saveOpenedAndUnopend() throws JsonProcessingException {
        acServiceNew.getDataFromACAndSaveToDB();
        return ResponseEntity.ok("ok");
    }

    @GetMapping("unsent")
    ResponseEntity<LocalDateTime> t() throws JsonProcessingException {
        LocalDateTime a = acServiceNew.getTimestampForContactThatIsNotInMap("5649",100);
        return ResponseEntity.ok(a);
    }



    @Scheduled(cron = "0 0 3 * * * ")
    @GetMapping("/getUnsubscriptions")
    ResponseEntity<String> getUnsubscriptions() throws JsonProcessingException {
        unsubscriptionsService.saveUnsubscriptionsToDB();
        return ResponseEntity.ok("saved");
    }

    @Autowired
    ACServiceNewImpl serviceNew;

    @GetMapping("/csv")
    ResponseEntity<List<String[]>> getCSVResponse() throws Exception {
        List<String[]> a = serviceNew.readLineByLine(Path.of("C:\\Users\\Matic\\Desktop\\listUnsub.csv"));
        for(String[] line : a) {
            System.out.println("idsub: " + line[0] + " date: " + line[1]);
        }
        return ResponseEntity.ok(a);
    }

    @GetMapping("getLink")
    ResponseEntity<String> getLinks() throws JsonProcessingException {
     List<Object[]> list = campaignRepository.getAllIdCampaignsAndIdMessage();

     for(Object[] a : list ){
         linksService.saveLinkIdToDB(a[0].toString(), a[1].toString());
     }
     return ResponseEntity.ok("ok");
    }
}