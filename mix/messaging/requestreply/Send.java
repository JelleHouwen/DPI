package messaging.requestreply;


import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;

public class Send {

	public static void sendObject(Serializable objToSend, String queueName) throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		Connection conn = factory.createConnection();
		conn.start();

		Session session = conn.createSession(false,Session.AUTO_ACKNOWLEDGE);
		Destination dest = session.createQueue(queueName);
		MessageProducer prod = session.createProducer(dest);
		prod.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		ObjectMessage message  = session.createObjectMessage(objToSend);

		prod.send(message);
		session.close();
		conn.close();
	}
}
