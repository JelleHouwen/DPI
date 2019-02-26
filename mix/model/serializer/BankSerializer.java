package model.serializer;


import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

public class BankSerializer implements ISerializer<BankInterestRequest, BankInterestReply> {

    @Override
    public String requestToString(BankInterestRequest request) {
        return GENSON.serialize(request);
    }

    @Override
    public BankInterestRequest requestFromString(String str) {
        return GENSON.deserialize(str, BankInterestRequest.class);
    }

    @Override
    public String replyToString(BankInterestReply reply) {
        return GENSON.serialize(reply);
    }

    @Override
    public BankInterestReply replyFromString(String str) {
        return GENSON.deserialize(str, BankInterestReply.class);
    }

}