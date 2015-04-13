package pop.server;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;

/**
 *
 * @author Anmar Hindi
 */
public class ThreadSearcher {

    private Lock lock = new ReentrantLock();
    static volatile String lookUpThread = "";
    JFrame frame = new JFrame("Search for active Threads");
    JPanel panel = new JPanel();
    JTextArea textAr = new JTextArea();
    static volatile boolean newForum = false;
    ResultLogger resultLogger = new ResultLogger();
    ServerSocket server;
    ResultCommunicator rCommunicator;
    String name = "";

    public ThreadSearcher(int num, String name) {
        connector(num, name);
    }

    public synchronized void setForum(boolean bool) {
        newForum = bool;
    }

    public boolean getForum() {
        return newForum;
    }

    private void connector(int i, String name) {
        this.name = name;
        drawWindow();
        try {
            server = new ServerSocket(i);
            System.out.println("This is the" + name + " server, listening on port: " + i);
        } catch (IOException e) {
            //nothing done
        }

        resultLogger.start();

        Thread thread = new Thread(new ThreadText());

        thread.start();

        while (true) {
            try {
                Socket socket = server.accept();

                rCommunicator = new ResultCommunicator(socket, resultLogger);

                resultLogger.addResultCommunicator(socket, rCommunicator);

                rCommunicator.start();
            } catch (IOException e) {
                //
            }

        }
    }

    public void drawWindow() {
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textAr.setText("PORT_OPEN");
        panel.add(textAr);
        panel.setPreferredSize(new Dimension(400, 600));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

    }

    class ThreadText implements Runnable {

        @Override
        public void run() {
            while (true) {
                lock.lock();
                try {
                    textAr.setText("" + MainDomain.serverCollector.size());

                    if (rCommunicator.receivedM.contains("create")) {
                        KeywordSearchServer.newForum = true;
                        KeywordSearchServer.temp = rCommunicator.receivedM;
                        rCommunicator.receivedM = "";
                    }
                } catch (Exception e) {
                    // nothing done
                } finally {
                    lock.unlock();
                }

            }
        }
    }

}
