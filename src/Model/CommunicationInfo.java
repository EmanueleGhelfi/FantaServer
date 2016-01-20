package Model;

/**
 * Created by Emanuele on 20/01/2016.
 */
public class CommunicationInfo {

    String code;
    String info;


    public CommunicationInfo(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
