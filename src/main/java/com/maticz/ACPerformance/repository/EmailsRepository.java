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

    @Query(value = "  SELECT TOP 1 *\n" +
            "FROM AC_fact_emails\n" +
            "WHERE idsubscriber = :idSubscriber " +
            "AND idCampaign = :idCampaign " +
            "AND (\n" +
            "    CAST(importTimestamp AS DATE) BETWEEN CAST(DATEADD(DAY, -10, GETDATE()) AS DATE) AND CAST(GETDATE() AS DATE)\n" +
            "    OR (\n" +
            "        page = :page " +
            "        AND times_opened = 0\n" +
            "    )\n" +
            ")\n" +
            "ORDER BY \n" +
            "    CASE \n" +
            "        WHEN CAST(importTimestamp AS DATE) BETWEEN CAST(DATEADD(DAY, -10, GETDATE()) AS DATE) AND CAST(GETDATE() AS DATE) THEN 0\n" +
            "        ELSE 1\n" +
            "    END,\n" +
            "    importTimestamp DESC ", nativeQuery = true) // unopened page numb se lahko spreminja če odprejo mail !!!
    Optional<Emails> findByIdSubscriberAndIdCampaignAndPageAndImportTimestamp(
            @Param("idSubscriber") Integer idSubscriber,
            @Param("idCampaign") Integer idCampaign,
            @Param("page") Integer page);

    @Query(value = "select * from AC_fact_emails e " +
            "where e.idSubscriber = :idSubscriber and e.idCampaign = :idCampaign " +
            "and (CAST(e.opened AS date) = :openedDate OR " +
            "(e.opened is null AND cast(e.email_sent as date) between " +
            "cast(dateadd(month, -1, getdate()) as date) AND cast(getdate() as date))) ",
            nativeQuery = true)
    Optional<Emails> findByIdSubscriberAndIdCampaignAndOpened(
            @Param("idSubscriber") Integer idSubscriber,
            @Param("idCampaign") Integer idCampaign,
            @Param("openedDate") LocalDate openedDate
    );

    @Transactional
    @Modifying
    @Query(
            value = "update ac_fact_emails " +
                    "set times_opened = :timesOpened , opened = :opened , page = :page " +
                    "where idSubscriber = :idSubscriber and idCampaign = :idCampaign " +
                    " and (CAST(opened AS date) = :opened OR " +
                    "            (opened is null AND cast(email_sent as date) between " +
                    "           cast(dateadd(month, -1, getdate()) as date) AND cast(getdate() as date))) ", nativeQuery = true
    )
    void updateValuesByIdSubscriberAndIdCampaign(@Param("timesOpened") Integer timesOpened, @Param("opened") LocalDateTime opened,
                                                 @Param("idSubscriber") Integer idSubscriber, @Param("idCampaign") Integer idCampaign,
                                                @Param("page") Integer page);

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
            (value = " select * from (select count(*) cnt , email , string_agg(a.idcampaign, '|') within group (order by email) campaingList, cast(email_sent as date) date, string_agg(b.automation, '|') within group (order by email) automationList  \n" +
                    "                                        from AC_fact_emails a, AC_ref_campaigns b  \n" +
                    "                                        where isnull(b.ignoreMultipleEmails,0) <> 1  and a.idcampaign = b.idcampaign  \n" +
                    "                                        and b.automation is not null \n" +
                    "                                        group by email, cast(email_sent as date) ) a  \n" +
                    "                                        where date = cast(GETDATE() as date) and cnt  > 1 ", nativeQuery = true)
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

    @Query
            (value = " select case when dateFrom is null then (select create_date from AC_ref_campaigns where idcampaign = :idCampaign) else dateFrom end from (\n" +
                    "\tselect cast(dateadd(day,-14,max(email_sent)) as date) dateFrom from AC_fact_emails a\n" +
                    "\twhere times_opened = 0  \n" +
                    "    and a.idCampaign = :idCampaign ) a " , nativeQuery = true)
    String dateToSearchByForUnopenedClients(@Param("idCampaign") Integer idCampaign);

}