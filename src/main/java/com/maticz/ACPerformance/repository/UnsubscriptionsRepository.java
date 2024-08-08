package com.maticz.ACPerformance.repository;

import com.maticz.ACPerformance.model.Unsubscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnsubscriptionsRepository extends JpaRepository<Unsubscriptions,Integer> {

    @Query
            (value = "select case when max(page) = 1 then 1  " +
                    "  when max(page) is null then 1  " +
                    "   else max(page) end from AC_fact_unsubscribes " +
                    " where idCampaign = :idCampaign " , nativeQuery = true)
    Integer getMaxPageForUnsubscribitionByIdCampaign(@Param("idCampaign") Integer idCampaign);


    @Query(value = " select * from AC_fact_unsubscribes " +
            "where idSubscriber = :idSubscriber and idCampaign = :idCampaign " +
            "and page = :page ", nativeQuery = true)
    Optional<Unsubscriptions> findByIdSubscriberAndIdCampaignAndPage(@Param("idSubscriber") Integer idSubscriber,
                                                                     @Param("idCampaign") Integer idCampaign,
                                                                     @Param("page") Integer page);

}
