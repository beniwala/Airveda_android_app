package in.airveda.sensors.devicecommunication;

/**
 * Created by siddhartha on 15/11/15.
 */
public interface TelnetCallBack {
    public void onConnect();

    public void onFailure(int error);

    public void onMessageReceive(String msg);
}
