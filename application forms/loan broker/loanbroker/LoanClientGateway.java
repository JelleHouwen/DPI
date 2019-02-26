package loanbroker;


import java.util.HashMap;
import java.util.Map;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import gateway.MessageReceiverGateway;
import gateway.MessageSenderGateway;
import model.serializer.LoanSerializer;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import net.sourceforge.jeval.EvaluationException;


abstract class LoanClientGateway {

    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanSerializer serializer;

    private static final String LOANREQUEST_QUEUE= "LoanRequestQueue";

    private Map<LoanRequest, Message> tempStorage;

    public LoanClientGateway() {
        serializer = new LoanSerializer();
        tempStorage = new HashMap<>();
        try {
            sender = new MessageSenderGateway();
            receiver = new MessageReceiverGateway(LOANREQUEST_QUEUE);
            receiver.setListener((Message message) -> {
                System.out.println("Broker received message from client");
                try {
                    String body = ((TextMessage) message).getText();
                    LoanRequest request = serializer.requestFromString(body);
                    tempStorage.put(request, message);
                    onLoanRequestArrived(request);
                } catch (JMSException ex) {
                    System.out.println("Error while receiving loanreply");
                } catch (EvaluationException e) {
                    e.printStackTrace();
                }
            });
        } catch (JMSException ex) {
            System.out.println("Error while setting up message-gateways. Is ActiveMQ running?");
        }
    }

    public void sendLoanReply(LoanRequest request, LoanReply reply) {
        try {
            String body = serializer.replyToString(reply);
            Message replyMessage = sender.createTextMessage(body);
            Message requestMessage = tempStorage.get(request);


            String jmsid = requestMessage.getJMSMessageID();
            Destination replyAddress = requestMessage.getJMSReplyTo();
            if (requestMessage.getJMSMessageID() == null) {
                throw new NullPointerException("jmsid was not found in map in method sendBankReply");
            }
            replyMessage.setJMSCorrelationID(jmsid);
            sender.Send(replyAddress, replyMessage);
            tempStorage.remove(request);
        } catch (JMSException ex) {
            System.out.println("Failed to set correlation ID in sendLoanReply");
        }
    }

    /**
     * This method is called when a message is received. The corresponding
     * request is fetched by the app gateway.
     *
     * @param request contains the original request
     */
    abstract void onLoanRequestArrived(LoanRequest request) throws EvaluationException;
}