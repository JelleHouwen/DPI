package messaging.requestreply;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Receive {

	public static void ReceiveObject(String queueName, MessageListener listener) throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		factory.setTrustAllPackages(true);

		Connection conn = factory.createConnection();

		Session session = conn.createSession(false,Session.AUTO_ACKNOWLEDGE);

		Queue requestQueue = session.createQueue(queueName);

		MessageConsumer consumer = session.createConsumer(requestQueue);
		consumer.setMessageListener(listener);
		conn.start();
	}
}
