package loanbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import gateway.MessageReceiverGateway;
import gateway.MessageSenderGateway;
import model.serializer.BankSerializer;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;


abstract class BankAppGateway {

    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private BankSerializer serializer;
    private Map<String, BankInterestRequest> tempStorage;

    public BankAppGateway(String bank) {
        serializer = new BankSerializer();
        tempStorage = new HashMap<>();
        try {

                sender=new MessageSenderGateway(bank);

            receiver = new MessageReceiverGateway();
            // Set listener
            receiver.setListener((Message message) -> {
                System.out.println("Broker received message from bank");
                try {
                    String body = ((TextMessage) message).getText();
                    BankInterestReply reply = serializer.replyFromString(body);

                    String corrId = message.getJMSCorrelationID();
                    BankInterestRequest request = tempStorage.get(corrId);


                    tempStorage.remove(message.getJMSCorrelationID());
                    onBankReplyArrived(request, reply);
                } catch (JMSException ex) {
                    System.out.println("Error while receiving BankInterestReply");
                } catch (EvaluationException e) {
                    e.printStackTrace();
                }
            });
        } catch (JMSException ex) {
            System.out.println("Error while setting up message-gateways. Is ActiveMQ running?");
        }
    }

    public void sendBankRequest(BankInterestRequest request) {
        try {
            String body = serializer.requestToString(request);

                Message msg = sender.createTextMessage(body);
                msg.setJMSReplyTo(receiver.getDestination());

                // send and keep track of original message.
                sender.Send(msg);
                tempStorage.put(msg.getJMSMessageID(), request);


        } catch (JMSException ex) {
            System.out.println("Failed to get JMSMessageID in sendBankRequest");
        }
    }



    /**
     * This method is called when a message is received. The corresponding
     * request is fetched by the app gateway.
     *
     * @param request contains the original request
     * @param reply contains the reply
     */
    abstract public void onBankReplyArrived(BankInterestRequest request, BankInterestReply reply) throws EvaluationException;

}