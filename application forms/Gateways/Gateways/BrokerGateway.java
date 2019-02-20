package Gateways;

import loanbroker.LoanBrokerFrame;
import messaging.requestreply.QueueTypes;
import messaging.requestreply.Receive;
import messaging.requestreply.Send;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.*;

public class BrokerGateway {

	private LoanBrokerFrame frame;

	private Map<Integer,Integer> sentMessages = new HashMap<Integer,Integer>();
	private Map<Integer,List<BankInterestReply>> receivedMessages = new HashMap<Integer,List<BankInterestReply>>();
	public BrokerGateway(LoanBrokerFrame currentFrame){
		frame = currentFrame;
		ReceiveLoanRequest(QueueTypes.LoanRequest.toString());
		ReceiveBankReply(QueueTypes.BankReply.toString());
	}

	public void ReceiveLoanRequest(String QueueName){
		try {
			Receive.ReceiveObject(QueueName, new MessageListener() {
				public void onMessage(Message message) {
					if(message instanceof ObjectMessage){
						Serializable obj = null;
						try {
							obj = ((ObjectMessage) message).getObject();
						} catch (JMSException e) {
							e.printStackTrace();
						}
						if (obj instanceof LoanRequest ){
							LoanRequest request = (LoanRequest) obj;
							BankInterestRequest bankRequest = new BankInterestRequest(request.getAmount(),request.getTime(),request.getId());
							frame.add(request);
							int messagesSent = ProcessMessagesAndSendToBrokers(bankRequest,QueueName);
							sentMessages.put(bankRequest.getId(),messagesSent);
							frame.add(request,bankRequest);
						}
					}
				}
			});
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void ReceiveBankReply(String QueueName){
		try {
			Receive.ReceiveObject(QueueName, new MessageListener() {
				public void onMessage(Message message) {
					if(message instanceof ObjectMessage){
						Serializable obj = null;
						try {
							obj = ((ObjectMessage) message).getObject();
						} catch (JMSException e) {
							e.printStackTrace();
						}
						if(obj instanceof BankInterestReply){
							BankInterestReply reply = (BankInterestReply) obj;
							ProcessReceivedMessage(reply,QueueTypes.LoanReply.toString());
						}
					}
				}
			});
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void ProcessReceivedMessage(BankInterestReply reply, String queueName){
	if(receivedMessages.containsKey(reply.getId())){
		List<BankInterestReply> replies = receivedMessages.get(reply.getId());
		replies.add(reply);
		if(replies.size() == sentMessages.get(reply.getId())){
			replies.sort(Comparator.comparing(BankInterestReply::getInterest).reversed());
			LoanReply loanReply = new LoanReply(reply.getInterest(),reply.getQuoteId(),reply.getId());
			LoanRequest request = frame.getLoanRequest(reply.getId());
			frame.add(request,reply);
			SendMessage(loanReply,queueName);
			receivedMessages.remove(reply.getId());
			sentMessages.remove(reply.getId());
		}
		else{
			receivedMessages.put(reply.getId(),replies);
		}

	}
	else{
		if(sentMessages.get(reply.getId()) == 1){
			LoanReply loanReply = new LoanReply(reply.getInterest(),reply.getQuoteId(),reply.getId());
			LoanRequest request = frame.getLoanRequest(reply.getId());
			frame.add(request,reply);
			SendMessage(loanReply,queueName);
			sentMessages.remove(reply.getId());
		}
		else{
			List<BankInterestReply> replies = new ArrayList<>();
			replies.add(reply);
			receivedMessages.put(reply.getId(),replies);
		}
	}
	}

	public int ProcessMessagesAndSendToBrokers(BankInterestRequest request,String queueType){
		int repliesSent = 0;

			repliesSent++;
			SendMessage(request,queueType);

		return repliesSent;
	}

	public int ProcessMessagesAndSendToBrokersRecipient(BankInterestRequest request){
		int repliesSent = 0;

		repliesSent++;
		SendMessageRecipient(request);

		return repliesSent;
	}
	public void SendMessageRecipient(BankInterestRequest request){
		try {

			Send.sendObject(request,getBank(request));
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (EvaluationException e) {
			e.printStackTrace();
		}
	}
	public void SendMessage(Serializable obj,String queueType){
		try {
			Send.sendObject(obj,queueType);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public String getBank(BankInterestRequest request) throws EvaluationException {
		String ING       = "#{amount} <= 100000 && #{time} <= 10";
		String ABN_AMRO  = "#{amount} >= 200000 && #{amount} <= 300000  && #{time} <= 20";
		String RABO_BANK = "#{amount} <= 250000 && #{time} <= 15";

		Evaluator evaluator = new Evaluator(); // for evaluation of bank rules


		// set values of variables amount and time
		evaluator.putVariable("amount", Integer.toString(request.getAmount()));
		evaluator.putVariable("time", Integer.toString(request.getTime()));

		String result = evaluator.evaluate(ING); // evaluate ING rule
		boolean ingRule = result.equals("1.0"); // 1.0 means TRUE, otherwise it is FALSE

		String resultABN = evaluator.evaluate(ABN_AMRO); // evaluate ABN Amro rule
		boolean abnRule = resultABN.equals("1.0"); // 1.0 means TRUE, otherwise it is FALSE

		String resultRABO = evaluator.evaluate(RABO_BANK); // evaluate RaboBank rule
		boolean raboRule = resultRABO.equals("1.0"); // 1.0 means TRUE, otherwise it is FALSE

		if(ingRule){
			return "ING";
		}
		else if (abnRule){
			return "ABN";
		}
		else return "RABO";

	}

}
