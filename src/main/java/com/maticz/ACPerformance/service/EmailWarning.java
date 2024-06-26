package com.maticz.ACPerformance.service;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

@Service
public interface EmailWarning {

    void sendEmailWarningWhenClientsReceiveMoreThenOneEmail() throws MessagingException;


}
