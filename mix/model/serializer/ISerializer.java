package model.serializer;
import com.owlike.genson.Genson;

public interface ISerializer<REQUEST, REPLY> {

    Genson GENSON = new Genson();

    public String requestToString(REQUEST request);

    public REQUEST requestFromString(String str);

    public String replyToString(REPLY reply);

    public REPLY replyFromString(String str);
}