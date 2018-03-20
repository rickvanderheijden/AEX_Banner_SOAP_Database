package shared;

import client.RequestType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class Request {
    @XmlElement(nillable = true)  // Important one. If not specified, application will crash with nullpointer.
    private List<String> fondsen;
    private RequestType requestType;

    public void setRequestedFondsen(List<String> fondsen) {
        this.fondsen = fondsen;
    }

    public List<String> getRequestFondsen() {
        return fondsen;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }
}
