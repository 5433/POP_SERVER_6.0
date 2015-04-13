package pop.server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 
 * @author Anmar Hindi
 */
public class ResultCommunicator extends Thread {

    private ArrayList messagesToSend = new ArrayList();
    private ResultLogger resultLogger;
    private PrintWriter printer;
    private Socket socket;
    private BufferedReader br;
    String receivedM = "";
    int threadPort;
    int deviceAddress;

    public ResultCommunicator(Socket s, ResultLogger ml) throws IOException {
        resultLogger = ml;
        socket = s;
        printer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ChoiceListener choiceListener = new ChoiceListener();
        choiceListener.start();
    }

    public String getMessage() {
        return receivedM;
    }

    //adds String message to arraylist messagesToSend.
    public synchronized void addToMessages(String text) {
        messagesToSend.add(text);
        notify();
    }

    //removes message from arraylist, and returns String message.
    private synchronized String nextElementToSend() throws InterruptedException {
        while (messagesToSend.size() == 0) {
            wait();
        }

        String message = (String) messagesToSend.get(0);
        messagesToSend.remove(0);
        return message;
    }

    //send String message to Device using PrintWriter.
    private void sendMessageToDevice(String text) {
        if (threadPort > 7777) {
            printer.println(threadPort + "\t");
        }
        printer.println(text);
        printer.flush();
        
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                resultLogger.postMessage();
                String message = nextElementToSend();
                sendMessageToDevice(message);
                //System.out.println(threadPort);
            }
        } catch (Exception e) {
            //
        }

        resultLogger.getResultCommunicator(socket).interrupt();
        resultLogger.removeResultCommunicator(socket);
    }

    class ChoiceListener extends Thread {

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    String message = br.readLine();
                    //messageM += message + "\r\n";
                    receivedM = message;
                    for (int i = 0; i < MainDomain.serverCollector.size(); i++) {
                        KeywordSearchServer temp = MainDomain.serverCollector.get(i);
                        if (temp.name.equals(message)) {
                            threadPort = temp.port;
                            //resultLogger.setPort("" + threadPort);
                        }
                    }
                }
            } catch (IOException ioex) {
                // could not read from socket
            }

            // Broken. Interrupt both listener and sender threads.
            resultLogger.getResultCommunicator(socket).interrupt();
            resultLogger.removeResultCommunicator(socket);

        }
    }

}
