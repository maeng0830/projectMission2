package com.maeng0830.account.controller;

import com.maeng0830.account.aop.AccountLock;
import com.maeng0830.account.dto.CancelBalance;
import com.maeng0830.account.dto.QueryTransactionResponse;
import com.maeng0830.account.dto.UseBalance;
import com.maeng0830.account.exception.AccountException;
import com.maeng0830.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // 잔액 사용
    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request) throws InterruptedException {
        try {
            Thread.sleep(3000L);
            return UseBalance.Response.from(transactionService.useBalance(
                    request.getUserId(), request.getAccountNumber(), request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to use balance.");
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(), request.getAmount()
            );

            throw e;
        }
    }

    // 잔액 사용 취소
    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request) {
        try {
            return CancelBalance.Response.from(transactionService.cancelBalance(
                    request.getTransactionId(), request.getAccountNumber(), request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to cancel use balance.");
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(), request.getAmount()
            );

            throw e;
        }
    }

    // 거래 확인
    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable String transactionId) {
       return  QueryTransactionResponse.from(
               transactionService.queryTransaction(transactionId)
       );
    }
}
