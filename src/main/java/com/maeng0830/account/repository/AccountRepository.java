package com.maeng0830.account.repository;

import com.maeng0830.account.domain.Account;
import com.maeng0830.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Repository 타입으로 Bean 등록
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Account <- 해당 인터페이스가 활용하게 될 엔티티
    // Long <- 엔티티 PK의 타입
    Optional<Account> findFirstByOrderByIdDesc();

    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String AccountNumber);

    List<Account> findByAccountUser(AccountUser accountUser);
}
