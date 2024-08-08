package com.maticz.ACPerformance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="AC_ref_campaigns")
public class Campaign {

    @Id
    @Column(name = "idCampaign")
    private Integer idCampaign;

    @Column(name = "name")
    private String name;

    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "send_amount")
    private Integer sendAmount;

    @Column(name = "total_send_amount")
    private Integer totalSendAmount;

    @Column(name = "opens")
    private Integer opens;

    @Column(name = "unique_opens")
    private Integer uniqueOpens;

    @Column(name = "link_clicks")
    private Integer linkClicks;

    @Column(name = "unique_clicks")
    private Integer uniqueClicks;

    @Column(name = "subscriber_clicks")
    private Integer subscriberClicks;

    @Column(name = "forwards")
    private Integer forwards;

    @Column(name = "hard_bounces")
    private Integer hardBounces;

    @Column(name = "soft_bounces")
    private Integer softBounces;

    @Column(name = "unsubscribes")
    private Integer unsubscribes;

    @Column(name = "unsubreasons")
    private Integer unsubreasons;

    @Column(name = "update_date")
    private LocalDate updateDate;

    @Column(name = "verified_opens")
    private Integer verifiedOpens;

    @Column(name = "verified_unique_opens")
    private Integer verifiedUniqueOpens;

    @Column(name = "idMessage")
    private Integer idMessage;

    @Column(name = "last_send_date")
    private LocalDateTime lastSendDate;

    @Column(name = "automation")
    private String automation;

    @Column(name = "import_to_fact_emails")
    private Integer importToFactEmails;

    @Column(name = "ignoreMultipleEmails")
    private Integer ignoreMultipleEmails;

    @Column(name = "AttractionRelated")
    private Integer attractionRelated;

    @Column(name = "ReportRelated")
    private Integer reportRelated;
}
