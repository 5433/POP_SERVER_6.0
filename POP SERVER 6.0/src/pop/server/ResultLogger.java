package pop.server;

import java.net.*;
import java.util.*;

/**
 * 
 * @author Anmar Hindi
 */
public class ResultLogger extends Thread {

    public volatile String castMessage = "";
    HashMap<Socket, ResultCommunicator> deviceIP = new HashMap<>();
    public ArrayList texts = new ArrayList();
    String port= "";

    //adds remote device to arraylist remoteDevices
    public synchronized void addResultCommunicator(Socket s, ResultCommunicator c) {
        deviceIP.put(s, c);
    }

    //removes remote device from arraylist remoteDevices.
    public synchronized void removeResultCommunicator(Socket s) {
        deviceIP.remove(s);
    }

    //String message is added to ArrayList messages, ArrayList reader awoken by notify().
    public synchronized void postMessage() {
        String a = "";
        for (int i = 0; i < MainDomain.serverCollector.size(); i++) {            
            a += MainDomain.serverCollector.get(i).name + "\t";            
        }
        texts.add(a);
        //texts.add(port);
        notify();
    }
    
    
    public void setPort(String p) {
        port = p;
    }

    public ResultCommunicator getResultCommunicator(Socket s) {
        return deviceIP.get(s);
    }

    //deletes a retrieved message from the ArrayList, then is returned.
    public synchronized String getNextElementFromMessages() throws InterruptedException {
        while (texts.size() == 0) {
            wait();
        }

        String message = (String) texts.get(0);
        texts.remove(0);
        return message;
    }

    //all remote devices will be sent a String message.
    private synchronized void echoAllDevices(String text) {
        Collection collection = deviceIP.values();
        Iterator it = collection.iterator();
        ResultCommunicator c;
        while (it.hasNext()) {
            c = (ResultCommunicator) it.next();
            c.addToMessages(text);
        }
    }

    //while loop allows for messages to be read and sent to all clients.
    @Override
    public void run() {
        try {
            while (true) {
                String message = getNextElementFromMessages();
                echoAllDevices(message);
                castMessage = message;
            }
        } catch (InterruptedException ie) {
        }
    }
}
