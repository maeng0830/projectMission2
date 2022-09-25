package com.maeng0830.account.repository;

import com.maeng0830.account.domain.Account;
import com.maeng0830.account.domain.AccountUser;
import com.maeng0830.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Repository 타입으로 Bean 등록
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
}
