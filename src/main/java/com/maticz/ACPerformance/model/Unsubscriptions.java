package com.maticz.ACPerformance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="AC_fact_unsubscribes")
public class Unsubscriptions {

    @Column(name="idSubscriber")
    private Integer idSubscriber;
    
    @Column(name="idCampaign")
    private Integer idCampaign;
    
    @Column(name="page")
    private Integer page;
    
    @Column(name="unsubscriptionDate")
    private LocalDateTime unSubscriptionDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email")
    private String email;
    
}
