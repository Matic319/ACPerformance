package com.maticz.ACPerformance.repository;

import com.maticz.ACPerformance.model.Campaign;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign,Integer> {

    Optional<Campaign> findByIdCampaign(Integer idCampaign);

    @Query(value = "select isnull(ignoreMultipleEmails,0) from AC_ref_campaigns where idCampaign = :idCampaign", nativeQuery = true)
    Integer ignoreListValue(@Param("idCampaign") Integer idCampaign);

    @Query(value = "select isnull(import_to_fact_emails,1) from AC_ref_campaigns where idCampaign = :idCampaign", nativeQuery = true)
    Integer importToFactValue(@Param("idCampaign") Integer idCampaign);

    @Query(value = "select isnull(reportRelated,1) from AC_ref_campaigns where idCampaign = :idCampaign", nativeQuery = true)
    Integer reportRelatedValue(@Param("idCampaign") Integer idCampaign );

    @Query(value = "select isnull(attractionRelated,0) from AC_ref_campaigns where idCampaign = :idCampaign", nativeQuery = true)
    Integer attractionRelatedValue(@Param("idCampaign") Integer idCampaign );


    @Query(value = "select idCampaign  from AC_ref_campaigns \n" +
            "where cast(last_send_date as date) >= dateadd(day,-7,cast(GETDATE()as date)) " +
            "and isnull(import_to_fact_emails,1) = 1 ", nativeQuery = true)
    List<Integer> listOfCampaignsSentInLastWeekWithoutSome();


    @Query(value = "select idCampaign, idMessage, *   from AC_ref_campaigns \n" +
            "where cast(last_send_date as date) >= dateadd(day,-7,cast(GETDATE() as date))" +
            " and isnull(import_to_fact_emails,1) <> 2 "
            , nativeQuery = true)
    List<Object[]> listOfIdsAndIdMessagesForCampaingsSentInLast7Days();

    Optional<Campaign> findByIdCampaignAndIdMessage(Integer idCampaign, Integer idMessage);

    @Transactional
    @Modifying
    @Query(value = "update AC_ref_campaigns \n" +
            "set \n" +
            "send_amount = :sendAmount,\n" +
            "total_send_amount = :totalSendAmount,\n" +
            "opens = :opens , \n" +
            "unique_opens = :uniqueOpens,\n" +
            "link_clicks = :linkClicks,\n" +
            "unique_clicks = :uniqueClicks,\n" +
            "forwards = :forwards ,\n" +
            "hard_bounces = :hardBounces,\n" +
            "soft_bounces = :softBounces,\n" +
            "unsubscribes = :unsubscribes,\n" +
            "unsubreasons = :unsubreasons,\n" +
            "update_date = convert(date,:updateDate,23),\n" +
            "verified_opens = :verifiedOpens,\n" +
            "verified_unique_opens = :verifiedUniqueOpens,\n" +
            "last_send_date = convert(datetime,:lastSendDate ,120)\n" +
            "where idCampaign = :idCampaign and idMessage = :idMessage " , nativeQuery = true)
    void updateBasedOnIdCampaignAndIdMessage(
            @Param("sendAmount") Integer sendAmount,
            @Param("totalSendAmount") Integer totalSendAmount,
            @Param("opens") Integer opens,
            @Param("uniqueOpens") Integer uniqueOpens,
            @Param("linkClicks") Integer linkClicks,
            @Param("uniqueClicks") Integer uniqueClicks,
            @Param("forwards") Integer forwards,
            @Param("hardBounces") Integer hardBounces,
            @Param("softBounces") Integer softBounces,
            @Param("unsubscribes") Integer unsubscribes,
            @Param("unsubreasons") Integer unsubreasons,
            @Param("updateDate") LocalDate updateDate,
            @Param("verifiedOpens") Integer verifiedOpens,
            @Param("verifiedUniqueOpens") Integer verifiedUniqueOpens,
            @Param("lastSendDate") LocalDateTime lastSendDate,
            @Param("idCampaign") Integer idCampaign,
            @Param("idMessage") Integer idMessage
    );

    @Query(
            value = "select idSubscriber , idCampaign , idMessage  from AC_fact_emails afe \n" +
                    "where times_opened = 0 and idCampaign in (892\n" +
                    ",893\n" +
                    ",894\n" +
                    ",895\n" +
                    ",896\n" +
                    ",897) and email_sent is null", nativeQuery = true
    )
    List<Object[]> getIdsOfSubscriberCampaignAndMessageForUnoped();

    @Transactional
    @Modifying
    @Query(value = "update AC_fact_emails \n" +
            "set email_sent = convert(datetime,:email_sent ,120)\n" +
            "where idCampaign = :idCampaign and idSubscriber = :idSubscriber  and idMessage = :idMessage " , nativeQuery = true
    )
    void updateUnsentTimeStampForIdSubscriber(
            @Param("email_sent") LocalDateTime email_sent,
            @Param("idCampaign") Integer idCampaign,
            @Param("idSubscriber") Integer idSubscriber,
            @Param("idMessage") Integer idMessage
    );



    @Query(
            value = "select idMessage from ac_ref_campaigns where idcampaign =  :id"
            ,nativeQuery = true )
    Integer getIdMessageForCampaign(@Param("id") Integer id);

    @Query
            (value =" select idCampaign from AC_ref_campaigns ", nativeQuery = true)
    List<Object[]> getAllIdCampaigns();
}
