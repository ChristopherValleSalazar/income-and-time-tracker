package com.chrisV.hoursBackend.repo;

import com.chrisV.hoursBackend.model.AmazonNames;
import com.chrisV.hoursBackend.model.AmazonTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmazonTransactionRepo extends JpaRepository<AmazonTransaction, Long> {

    @Query("SELECT DISTINCT person FROM AmazonTransaction")
    List<AmazonNames> findAmazonTransactionNames();


}
