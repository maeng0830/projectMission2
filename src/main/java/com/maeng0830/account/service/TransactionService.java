package com.maeng0830.account.service;

import com.maeng0830.account.domain.Account;
import com.maeng0830.account.domain.AccountUser;
import com.maeng0830.account.domain.Transaction;
import com.maeng0830.account.dto.TransactionDto;
import com.maeng0830.account.exception.AccountException;
import com.maeng0830.account.repository.AccountRepository;
import com.maeng0830.account.repository.AccountUserRepository;
import com.maeng0830.account.repository.TransactionRepository;
import com.maeng0830.account.type.AccountStatus;
import com.maeng0830.account.type.TransactionResultType;
import com.maeng0830.account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.maeng0830.account.type.ErrorCode.*;
import static com.maeng0830.account.type.TransactionResultType.F;
import static com.maeng0830.account.type.TransactionResultType.S;
import static com.maeng0830.account.type.TransactionType.CANCEL;
import static com.maeng0830.account.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    // 잔액 사용
    @Transactional
    public TransactionDto useBalance(Long userId,
                                     String accountNumber, Long amount) {
        // 사용자가 없는 경우
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        // 계좌가 없는 경우
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        // 사용자와 계좌 불일치, 이미 해지된 계좌, 거래 금액이 잔액을 초과
        validateUseBalance(user, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(USE, S, account, amount));
    }

    // 잔액 사용 - 사용자와 계좌 불일치, 이미 해지된 계좌, 거래 금액이 잔액을 초과
    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        // 사용자와 계좌 불일치
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        // 이미 해지된 계좌
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 거래 금액이 잔액을 초과
        if (account.getBalance() < amount) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(USE, F, account, amount);
    }

    private Transaction saveAndGetTransaction(
            TransactionType transactionType,
            TransactionResultType transactionResultType, Account account, Long amount) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }

    // 잔액 사용 취소
    @Transactional
    public TransactionDto cancelBalance(
            String transactionId,
            String accountNumber,
            Long amount) {
        // 거래가 없는 경우
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));

        // 계좌가 없는 경우
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        // 거래와 계좌 불일치, 부분 취소 불가
        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                saveAndGetTransaction(CANCEL, S, account, amount));
    }

    // 잔액 사용 취소 - 거래와 계좌 불일치, 부분 취소 불가
    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        // 거래와 계좌 불일치
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(TRANSACTION_ACCOUNT_UN_MATCH);
        }

        // 부분 취소 불가
        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(CANCEL_MUST_FULLY);
        }
    }

    @Transactional
    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(CANCEL, F, account, amount);
    }

    // 거래 확인
    public TransactionDto queryTransaction(String transactionId) {
        // 거래가 없는 경우
        return TransactionDto.fromEntity(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND)));
    }
}
