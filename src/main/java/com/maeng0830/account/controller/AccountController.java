package com.maeng0830.account.controller;

import com.maeng0830.account.domain.Account;
import com.maeng0830.account.dto.AccountInfo;
import com.maeng0830.account.dto.CreateAccount;
import com.maeng0830.account.dto.DeleteAccount;
import com.maeng0830.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController // RestController로 Bean 등록
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    // 계좌 생성
    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
    ) {

        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(),
                        request.getInitialBalance()));
    }

    // 계좌 해지
    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request
    ) {

        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(),
                        request.getAccountNumber()));
    }

    // 계좌 조회
    @GetMapping("/account")
    public List<AccountInfo> getAccountByUserId(
            @RequestParam("user_id") Long userId
    ) {
        return accountService.getAccountsByUserId(userId)
                .stream().map(accountDto -> AccountInfo.builder()
                        .accountNumber(accountDto.getAccountNumber())
                        .balance(accountDto.getBalance())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/account/{id}")
    public Account getAccount(
            @PathVariable Long id) {
        return accountService.getAccount(id);
    }

}
