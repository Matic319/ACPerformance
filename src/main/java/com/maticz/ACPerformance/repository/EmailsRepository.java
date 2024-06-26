package com.maticz.ACPerformance.repository;

import com.maticz.ACPerformance.model.Emails;
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
public interface EmailsRepository extends JpaRepository<Emails,Integer> {


    Optional<Emails> findByIdSubscriberAndIdCampaign(Integer idSubscriber, Integer idCampaign);

    Optional<Emails> findByIdSubscriberAndIdCampaignAndPage(Integer idSubscriber, Integer idCampaign, Integer page);

    Optional<Emails> findByIdSubscriberAndIdCampaignAndOpened(Integer idSubscriber, Integer idCampaign, LocalDate opened);

    @Transactional
    @Modifying
    @Query(
            value = "update ac_fact_emails " +
                    "set times_opened = :timesOpened , opened = :opened " +
                    "where idSubscriber = :idSubscriber and idCampaign = :idCampaign", nativeQuery = true
    )
    void updateValuesByIdSubscriberAndIdCampaign(@Param("timesOpened") Integer timesOpened, @Param("opened") LocalDateTime opened,
                                                 @Param("idSubscriber") Integer idSubscriber, @Param("idCampaign") Integer idCampaign);

    @Query(
            value = "select case when max(page) = 1 then 1 " +
                    " when max(page) is null then 1 " +
                    " else max(page)-1 end from AC_fact_emails" +
                    " where idCampaign = :idCampaign and times_opened > 0" ,nativeQuery = true
    )
    Integer getPageNumberForCampaignByIdTimesOpenedGreaterThen0(@Param("idCampaign") int idCampaign);

    @Query(
            value = "select case when max(page) = 1 then 1 " +
                    " when max(page) is null then 1 " +
                    " else max(page)-1 end from AC_fact_emails" +
                    " where idCampaign = :idCampaign and times_opened = 0" ,nativeQuery = true
    )
    Integer getPageNumberForCampaignByIdTimesOpenedEquals0(@Param("idCampaign") int idCampaign);





    @Transactional
    @Modifying
    @Query
            (value = " update AC_fact_emails " +
                    " set email_sent = convert(datetime,:email_sent ,120) " +
                    " where idCampaign = :idCampaign and idsubscriber = :idSubscriber and email_sent is null " , nativeQuery = true )
    void updateClientsEmailSendDateWhereEmailSentIsNull(
            @Param("email_sent") LocalDateTime email_sent,
            @Param("idCampaign") Integer idCampaign,
            @Param("idSubscriber") Integer idSubscriber
    );

    @Transactional
    @Modifying
    @Query
            (value = " update AC_fact_emails " +
                    " set email_sent = convert(datetime,:email_sent ,120) " +
                    " where idCampaign = :idCampaign and email_sent is null " , nativeQuery = true )
    void updateEveryClientsEmailSendDateBasedOnIdCampaignAndSentDateNull(
            @Param("email_sent") LocalDateTime email_sent,
            @Param("idCampaign") Integer idCampaign
    );


    @Transactional
    @Modifying
    @Query
            (value = " update AC_fact_emails " +
                    " set email_sent = convert(datetime,:email_sent ,120) " +
                    " where idCampaign = :idCampaign and email_sent is null and cast(importTimestamp as date) = cast(getdate() as date ) " , nativeQuery = true )
    void updateEveryClientsEmailSendDateBasedOnIdCampaignAndSentDateNullAndCurrentDate(
            @Param("email_sent") LocalDateTime email_sent,
            @Param("idCampaign") Integer idCampaign
    );

    @Query
            (value =  "select a.idSubscriber ,a.idCampaign ,a.opened , b.create_date, COALESCE(dateadd(day,-7,a.opened),b.create_date) dateToSearchBy\n" +
                    "from AC_fact_emails a left join AC_ref_campaigns b on a.idCampaign = b.idCampaign \n" +
                    "where email_sent is null \n" , nativeQuery = true )
    List<Object[]> getIdSubscribersWithoutEmailSent();

    @Query
            (value = " select min(a.dateToSearchBy), a.idSubscriber , b.cnt , b.allCampaigns from (\n" +
                    "select a. idSubscriber ,a. idCampaign ,a. opened , b. create_date, COALESCE(dateadd(day,-7,a. opened),b. create_date) dateToSearchBy \n" +
                    "from AC_fact_emails a left join AC_ref_campaigns b on a. idCampaign = b. idCampaign\n" +
                    "where email_sent is null\n" +
                    ") a \n" +
                    "left join \n" +
                    "(select count(*) cnt, idsubscriber,  string_agg(idCampaign, '|')  within group (order by idsubscriber) allCampaigns from ac_fact_emails where email_sent is null  group by idsubscriber )  b \n" +
                    "\ton a.idsubscriber = b.idsubscriber\n" +
                    "\t\tgroup by a.idsubscriber, b.cnt , b.allCampaigns " , nativeQuery = true)
    List<Object[]> getIdSubscriberWithountEmailSentWithConcatenatedIdCampaign();

    @Query
            (value = " select * from (select count(*) cnt , email , string_agg(a.idcampaign, '|') within group (order by email) campaingList, cast(email_sent as date) date, string_agg(b.automation, '|') within group (order by email) automationList \n" +
                    "                    from AC_fact_emails a, AC_ref_campaigns b \n" +
                    "                    where a.idcampaign not in (437,438,439,440,442,443,445,447,449,548,549,550,551,552,554,555,557,960 \n" +
                    "                    ,961,963,965,967,969,1071,1073,1078,1080,1082,1084, 182, 1011, 1013, 516, 491, 522, 560 , 561, 562, 572\n" +
                    "                    ,195,197,198,351,435,491,827,828,841,842, 573) and a.idcampaign = b.idcampaign \n" +
                    "                    and b.automation is not null\n" +
                    "                    group by email, cast(email_sent as date) ) a \n" +
                    "                    where date = cast(GETDATE() as date) and cnt  > 1", nativeQuery = true)
    List<Object[]> getAllClientsThatReceivedMultipleEmailsInTheSameDay();


    @Query
            (value = " select 52, 516 from (\n" +
                    "select count(*) st, page, idCampaign from AC_fact_emails \n" +
                    "where opened > 0 and idCampaign in (516,522,572,560,1227) \n" +
                    "group by page, idCampaign \n" +
                    ") a \n" +
                    "where st < 20\n" +
                    "group by idCampaign " , nativeQuery = true)
    List<Object[]> getMinPageWhereCountLessThen20ForBdayPhotos();

}