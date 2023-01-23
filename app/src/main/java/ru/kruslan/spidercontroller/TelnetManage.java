package ru.kruslan.spidercontroller;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class TelnetManage {
    private static TelnetManage telnetManage;
    private boolean boo = false;
    private TelnetClient telnetClient;
    private InputStream in = null;
    private OutputStream out = null;
    private Map<String, TelnetMsgCall> telnetMsgCallMap = new HashMap<>();

    public static TelnetManage getInstance() {
        if (telnetManage == null)
            telnetManage = new TelnetManage();
        return telnetManage;
    }

    public void connect(final String ip, final int port) {
        if (!boo) {
            boo = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        telnetClient = new TelnetClient();
                        telnetClient.connect(ip, port);
                        if (telnetMsgCallMap != null) {
                            for (TelnetMsgCall telnetMsgCall : telnetMsgCallMap.values()) {
                                telnetMsgCall.onConnectState(telnetClient.isConnected());
                            }
                        }
                        in = telnetClient.getInputStream();
                        out = telnetClient.getOutputStream();
                        new ReadThread().start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (telnetMsgCallMap != null) {
                            for (TelnetMsgCall telnetMsgCall : telnetMsgCallMap.values()) {
                                telnetMsgCall.onConnectState(false);
                            }
                        }
                    }
                }
            }).start();
        }
    }

    public void sendMsg(final String msg) {
        if (out != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        out.write(msg.getBytes());
                        out.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            try {
                while (boo && in != null) {
                    byte[] data = new byte[1024];
                    int len = in.read(data);
                    if (len != -1) {
                        String msg = new String(data, 0, len);
                        if (telnetMsgCallMap != null) {
                            for (TelnetMsgCall telnetMsgCall : telnetMsgCallMap.values()) {
                                telnetMsgCall.onMessage(msg);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public TelnetClient getTelnetClient() {
        return telnetClient;
    }

    public void disconnect() {
        try {
            if (telnetClient != null) {
                telnetClient.disconnect();
            }
            boo = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface TelnetMsgCall {

        void onMessage(String msg);

        void onConnectState(boolean isConnected);

    }

    public void addTelnetMsgCall(String key, TelnetMsgCall telnetMsgCall) {
        telnetMsgCallMap.put(key, telnetMsgCall);
    }

    public void removeTelnetMsgCall(String key) {
        telnetMsgCallMap.remove(key);
    }

}
