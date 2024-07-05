package com.maticz.ACPerformance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maticz.ACPerformance.model.Campaign;
import com.maticz.ACPerformance.service.ACServiceNew;
import com.maticz.ACPerformance.service.impl.ACServiceImpl;
import com.maticz.ACPerformance.service.impl.EmailWarningImpl;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Scheduled(cron = "0 0 */2 * * *")
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
    ResponseEntity<String> sendEmail13() throws MessagingException {
        emailWarning.sendEmailWarningWhenClientsReceiveMoreThenOneEmail();
        return ResponseEntity.ok("ok");
    }

    @Scheduled(cron = "0 0 17 * * * ")
    @GetMapping("/sendMail2")
    ResponseEntity<String> sendEmail17() throws MessagingException {
        emailWarning.sendEmailWarningWhenClientsReceiveMoreThenOneEmail();
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/p")
    ResponseEntity<String> test51() throws JsonProcessingException {
        acService.saveContactsWhereDataIsMissingForPhotos();
        return ResponseEntity.ok("ok");
    }

    @GetMapping("test")
    ResponseEntity<String> abv() throws JsonProcessingException {
        acService.izbrisPol();
        return ResponseEntity.ok("ok");
    }


    @GetMapping("test2")
    ResponseEntity<String> abv2() throws JsonProcessingException {
        acServiceNew.getDataFromACAndSaveToDB();
        return ResponseEntity.ok("ok");
    }
}