package model.bank;

import model.loan.LoanRequest;

import java.io.Serializable;

/**
 *
 * This class stores all information about an request from a bank to offer
 * a loan to a specific client.
 */
public class BankInterestRequest implements Serializable{

    private int amount; // the requested loan amount
    private int time; // the requested loan period
    private int id;
    private String bankId;

    public BankInterestRequest() {
        super();
        this.amount = 0;
        this.time = 0;
        this.id=0;
    }

    public BankInterestRequest(int amount, int time,int id) {
        super();
        this.amount = amount;
        this.time = time;
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }


    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getId() {return id;}

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    @Override
    public String toString() {
        return " amount=" + amount + " time=" + time;
    }
}