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

    // �ܾ� ���
    @Transactional
    public TransactionDto useBalance(Long userId,
                                     String accountNumber, Long amount) {
        // ����ڰ� ���� ���
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        // ���°� ���� ���
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        // ����ڿ� ���� ����ġ, �̹� ������ ����, �ŷ� �ݾ��� �ܾ��� �ʰ�
        validateUseBalance(user, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(USE, S, account, amount));
    }

    // �ܾ� ��� - ����ڿ� ���� ����ġ, �̹� ������ ����, �ŷ� �ݾ��� �ܾ��� �ʰ�
    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        // ����ڿ� ���� ����ġ
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        // �̹� ������ ����
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        // �ŷ� �ݾ��� �ܾ��� �ʰ�
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

    // �ܾ� ��� ���
    @Transactional
    public TransactionDto cancelBalance(
            String transactionId,
            String accountNumber,
            Long amount) {
        // �ŷ��� ���� ���
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));

        // ���°� ���� ���
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        // �ŷ��� ���� ����ġ, �κ� ��� �Ұ�
        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                saveAndGetTransaction(CANCEL, S, account, amount));
    }

    // �ܾ� ��� ��� - �ŷ��� ���� ����ġ, �κ� ��� �Ұ�
    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        // �ŷ��� ���� ����ġ
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(TRANSACTION_ACCOUNT_UN_MATCH);
        }

        // �κ� ��� �Ұ�
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

    // �ŷ� Ȯ��
    public TransactionDto queryTransaction(String transactionId) {
        // �ŷ��� ���� ���
        return TransactionDto.fromEntity(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND)));
    }
}
