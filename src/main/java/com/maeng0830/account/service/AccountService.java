package com.maeng0830.account.service;

import com.maeng0830.account.domain.Account;
import com.maeng0830.account.domain.AccountUser;
import com.maeng0830.account.dto.AccountDto;
import com.maeng0830.account.exception.AccountException;
import com.maeng0830.account.repository.AccountRepository;
import com.maeng0830.account.repository.AccountUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.maeng0830.account.type.AccountStatus.IN_USE;
import static com.maeng0830.account.type.AccountStatus.UNREGISTERED;
import static com.maeng0830.account.type.ErrorCode.*;

@Service // Service 타입으로 Bean 등록
@RequiredArgsConstructor // final 필드만 매개변수로 갖는 생성자
public class AccountService {
    private final AccountRepository accountRepository; // final <- 무조건 생성자로 값을 대입해줘야하는 필드, Required
    private final AccountUserRepository accountUserRepository;

    // 계좌 생성
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        // 사용자 존재 여부 확인
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        // 사용자 당 계좌 개수 확인(최대 10개)
        validateCreateAccount(accountUser);

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        return AccountDto.fromEntity(accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));
    }

    // 계좌 생성 - 사용자 당 계좌 개수 확인(최대 10개)
    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        if (id < 0) {
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }

    // 계좌 해지
    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        // 사용자가 없는 경우
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        // 계좌가 없는 경우
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        // 사용자와 계좌 불일치, 이미 해지된 계좌, 잔액이 있는 계좌
        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    // 계좌 해지 - 사용자와 계좌 불일치, 이미 해지된 계좌, 잔액이 있는 계좌
    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        // 사용자와 계좌 불일치
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        // 이미 해지된 계좌
        if (account.getAccountStatus() == UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 잔액이 있는 계좌
        if (account.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
    }

    // 사용자의 계좌 확인
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        // 사용자가 없는 경우
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
}
