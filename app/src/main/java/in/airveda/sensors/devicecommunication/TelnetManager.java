package in.airveda.sensors.devicecommunication;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created by siddhartha on 15/11/15.
 */
public class TelnetManager {
    private String host;
    private int port;
    private TelnetCallBack callBack;

    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private StringBuffer sb;

    public TelnetManager(TelnetCallBack callBack){
        this.callBack = callBack;
    }

    public void connect(String host_in, int port_in){
        this.host = host_in;
        this.port = port_in;

        try {
            Thread mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        telnet.connect(host, port);
                        in = telnet.getInputStream();
                        out = new PrintStream(telnet.getOutputStream());
                        telnet.setKeepAlive(true);
                        callBack.onConnect();
                        sb = new StringBuffer();
                        int prevNewLine = 0;
                        while (true)
                        {
                            int len = in.read();
                            String s = Character.toString((char) len);
                            sb.append(s);
                            if(sb.toString().endsWith("\n")){
                                String tempCmd = sb.substring(prevNewLine,sb.length() - 1);
                                prevNewLine = sb.length();
                                callBack.onMessageReceive(tempCmd);
                            }
                        }
                    } catch (IOException e) {
                        callBack.onFailure(-1);
                        e.printStackTrace();
                    }
                }
            });
            mThread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void sendMessage(String msg){
        out.println(msg);
        out.flush();
    }

    public void disconnect() {
        try {
            in.close();
            out.close();
            telnet.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}


