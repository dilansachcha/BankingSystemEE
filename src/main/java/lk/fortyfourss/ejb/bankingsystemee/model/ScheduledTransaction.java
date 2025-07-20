package lk.fortyfourss.ejb.bankingsystemee.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "scheduled_transaction")
public class ScheduledTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "from_account", nullable = false)
    private String fromAccount;

    @Column(name = "to_account", nullable = false)
    private String toAccount;

    @Column(nullable = false)
    private double amount;

    @Column(name = "scheduled_time", nullable = false)
    private Timestamp scheduledTime;

    @Column(nullable = false)
    private boolean recurring;  // true = recurring transaction

    @Column(name = "recurrence_type", length = 20) // DAILY, WEEKLY, etc.
    private String recurrenceType;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "next_scheduled_time")
    private Timestamp nextScheduledTime;

    @Column(name = "last_executed")
    private Timestamp lastExecuted;

    public ScheduledTransaction() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Timestamp scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public int getRetryCount() {return retryCount;}

    public void setRetryCount(int retryCount) {this.retryCount = retryCount;}

    public Timestamp getNextScheduledTime() {return nextScheduledTime;}

    public void setNextScheduledTime(Timestamp nextScheduledTime) {this.nextScheduledTime = nextScheduledTime;}

    public Timestamp getLastExecuted() {return lastExecuted;}

    public void setLastExecuted(Timestamp lastExecuted) {this.lastExecuted = lastExecuted;}

}
