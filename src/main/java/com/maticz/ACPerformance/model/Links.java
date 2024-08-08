package com.maticz.ACPerformance.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "AC_ref_links")
public class Links {

    @Column(name = "idCampaign")
    private Integer idCampaign;

    @Column(name = "link")
    private String link;

    @Column(name="idLink")
    private Integer idLink;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name = "idMessage")
    private Integer idMessage;
}
