package model.bank;

import model.loan.LoanRequest;

import java.io.Serializable;

/**
 * This class stores information about the bank reply
 *  to a loan request of the specific client
 * 
 */
public class BankInterestReply implements Serializable{

    private double interest; // the loan interest
    private String bankId; // the nunique quote Id
    private int id;

    public BankInterestReply() {
        this.interest = 0;
        this.bankId = "";
        this.id = 0;
    }

    public BankInterestReply(double interest, String quoteId, int id) {
        this.interest = interest;
        this.bankId = quoteId;
        this.id = id;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getQuoteId() {
        return bankId;
    }

    public void setQuoteId(String quoteId) {
        this.bankId = quoteId;
    }

    public int getId() {return id;}

    public String toString() {
        return "quote=" + this.bankId + " interest=" + this.interest;
    }
}