package Gateways;

import loanclient.LoanClientFrame;
import messaging.requestreply.QueueTypes;
import messaging.requestreply.Receive;
import messaging.requestreply.RequestReply;
import messaging.requestreply.Send;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

public class ClientGateway  {

	public LoanClientFrame frame;

	public ClientGateway(LoanClientFrame currentFrame){
		frame = currentFrame;
		ReceiveMessage(QueueTypes.LoanReply.toString());

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
						if(obj instanceof LoanReply){
							LoanReply reply = (LoanReply) obj;
							LoanRequest request = frame.getLoanRequest(reply.getId());
							RequestReply<LoanRequest,LoanReply> rr = frame.getRequestReply(request);
							frame.addReply(rr,reply);

						}
					}
				}
			});
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void SendMessage(LoanRequest request){
		try {

			Send.sendObject(request, QueueTypes.LoanRequest.toString());
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}
}
