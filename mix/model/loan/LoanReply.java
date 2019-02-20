package model.loan;

import java.io.Serializable;

/**
 *
 * This class stores all information about a bank offer
 * as a response to a client loan request.
 */
public class LoanReply implements Serializable{

    private double interest; // the interest that the bank offers
    private String bankID; // the unique quote identification
    private int id;

    public LoanReply() {
        super();
        this.interest = 0;
        this.bankID = "";
        this.id = 0;
    }
    public LoanReply(double interest, String quoteID,int id) {
        super();
        this.interest = interest;
        this.bankID = quoteID;
        this.id = id;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getQuoteID() {
        return bankID;
    }

    public void setQuoteID(String quoteID) {
        this.bankID = quoteID;
    }

    public int getId() { return id;}
    @Override
    public String toString(){
        return " interest="+String.valueOf(interest) + " quoteID="+String.valueOf(bankID);
    }
}