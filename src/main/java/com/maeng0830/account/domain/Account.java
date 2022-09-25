package com.maeng0830.account.domain;

import com.maeng0830.account.exception.AccountException;
import com.maeng0830.account.type.AccountStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.maeng0830.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;
import static com.maeng0830.account.type.ErrorCode.INVALID_REQUEST;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity // 엔티티는 일종의 설정 클래스, DB의 테이블
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id // id를 Account라는 테이블의 PK로 지정한다.
    @GeneratedValue // 자동 값 생성: 1~n
    private Long id;

    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;

    @Enumerated(EnumType.STRING) // Enum은 사실 0 ~ n이기 때문에, 실제로 알아볼 수 있도록 String으로 변경해줌.
    private AccountStatus accountStatus;
    private Long balance;

    @CreatedDate
    private LocalDateTime registeredAt;
    @LastModifiedDate
    private LocalDateTime unRegisteredAt;

    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }

        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(INVALID_REQUEST);
        }

        balance += amount;
    }
}
