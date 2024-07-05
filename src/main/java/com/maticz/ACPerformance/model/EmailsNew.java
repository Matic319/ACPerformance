package com.maticz.ACPerformance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "AC_fact_emails_new")
public class EmailsNew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "idSubscriber")
    private Integer idSubscriber;

    @Column(name = "email")
    private String email;

    @Column(name = "idCampaign")
    private Integer idCampaign;

    @Column(name = "timesOpened")
    private Integer timesOpened;

    @Column(name = "opened")
    private LocalDateTime opened;

    @Column(name="page")
    private Integer page;

    @Column(name = "idMessage")
    private Integer idMessage;

    @Column(name ="emailSent")
    private LocalDateTime emailSent;

    @Column(name = "importTimestamp")
    private LocalDateTime importTimestamp;

    @Column(name= "nodeFieldNumber")
    private Integer nodeFieldNumber;
}
