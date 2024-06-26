package com.maticz.ACPerformance.service.impl;

import com.maticz.ACPerformance.repository.EmailsRepository;
import com.maticz.ACPerformance.service.EmailWarning;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class EmailWarningImpl implements EmailWarning {

    @Autowired
    EmailsRepository emailsRepository;

    @Autowired
    private JavaMailSender mailSender;

    LocalDate dateOfLastSend = null;

    @Override
    public void sendEmailWarningWhenClientsReceiveMoreThenOneEmail() throws MessagingException {
        List<Object[]> queryResults = emailsRepository.getAllClientsThatReceivedMultipleEmailsInTheSameDay();

        if (!queryResults.isEmpty() && !Objects.equals(dateOfLastSend, LocalDate.now())) {
            File csvFile = new File("list.csv");
            try (FileWriter writer = new FileWriter(csvFile);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                         "count", "email", "campaignList",
                         "date", "automationList"))) {

                for (Object[] row : queryResults) {
                    csvPrinter.printRecord(row);
                }
                csvPrinter.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error writing", e);
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            try {
                helper.setTo(new String[]{"matic.zigon@woop.fun", "ziga.doberlet@woop.fun"});
                helper.setSubject("AC preveč poslanih");
                helper.setText("AC");
                helper.addAttachment("list.csv", csvFile);
                mailSender.send(message);
                dateOfLastSend = LocalDate.now();
            } catch (MessagingException e) {
                throw new RuntimeException("Error  sending ", e);
            } finally {
                if (csvFile.exists() && !csvFile.delete()) {
                    System.err.println("Failed to delete ");
                }
            }
        }
    }
}

