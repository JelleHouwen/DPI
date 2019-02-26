package gateway;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public abstract class MessageAbstractGateway {

    private String queueName;

    private Connection connection;
    private Session session;
    private Destination destination;

    public MessageAbstractGateway(String brokerUrl, String queueName) throws JMSException {
        this.queueName = queueName;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (this.queueName.equals("temporary")) {
            this.queueName = null;
            this.destination = null;
        } else {

            destination = session.createQueue(this.queueName);
        }
    }

    public MessageAbstractGateway(String queueName) throws JMSException {
        this(ActiveMQConnection.DEFAULT_BROKER_URL, queueName);
    }

    protected void createTempQueueForReceiver() throws JMSException {
        TemporaryQueue tempq = session.createTemporaryQueue();
        this.queueName = tempq.getQueueName();
        this.destination = tempq;
    }


    public final Session getSession() {
        return session;
    }


    public final Destination getDestination() {
        return destination;
    }

}
