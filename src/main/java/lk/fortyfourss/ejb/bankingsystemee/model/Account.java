package lk.fortyfourss.ejb.bankingsystemee.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "user_id", insertable = true, updatable = true)
    private int userId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_type")
    private String accountType;

    private double balance;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String status;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "last_interest_date")
    private LocalDate lastInterestDate;

    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;

    @Column(name = "maturity_status")
    private String maturityStatus;

    @Column(name = "initial_deposit")
    private double initialDeposit;

    public Account() {}

    public Account(int userId, String accountNumber, String accountType, double balance) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Account(User user, String accountNumber, String accountType, double balance) {
        this.user = user;
        this.userId = user.getId();
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = "ACTIVE";
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) this.userId = user.getId();
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public LocalDate getLastInterestDate() {return lastInterestDate;}

    public void setLastInterestDate(LocalDate lastInterestDate) {this.lastInterestDate = lastInterestDate;}

    public LocalDateTime getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDateTime maturityDate) { this.maturityDate = maturityDate; }

    public String getMaturityStatus() { return maturityStatus; }
    public void setMaturityStatus(String maturityStatus) { this.maturityStatus = maturityStatus; }

    public double getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(double initialDeposit) { this.initialDeposit = initialDeposit; }

}
