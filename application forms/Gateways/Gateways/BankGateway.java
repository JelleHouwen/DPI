package Gateways;

import bank.JMSBankFrame;
import messaging.requestreply.QueueTypes;
import messaging.requestreply.Receive;
import messaging.requestreply.Send;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

public class BankGateway  {
	private JMSBankFrame frame;

	public BankGateway(JMSBankFrame currentFrame){
		frame = currentFrame;
		ReceiveMessage(QueueTypes.LoanRequest.toString());
	}
	public void ReceiveMessage(String QueueName){
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
						if(obj instanceof BankInterestRequest){
							BankInterestRequest request = (BankInterestRequest) obj;
							frame.addRequest(request);
						}
					}
				}
			});
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void SendMessage(BankInterestReply reply,String queueName){
		try {
			Send.sendObject(reply, queueName);
		} catch (JMSException e) {
			e.printStackTrace();
		}


	}
}
