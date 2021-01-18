package Threads;


import HttpConnect.HttpConnect;
import MVCBot.APIBoeBotController;

import static java.lang.Thread.sleep;

public class CommandThread implements Runnable {

    private APIBoeBotController boeBotController;
    private DriveThread driveThread;
    private DiagnoseThread diagnoseThread;
    private HttpConnect httpConnect;

    private Thread worker;

    public boolean running = true;

    public CommandThread(APIBoeBotController boeBotController, HttpConnect httpConnect) {
        this.boeBotController = boeBotController;
        this.httpConnect = httpConnect;
    }

    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    @Override
    public void run() {
        //* Useage of the while running = true loop is to be able to stop the thread with running = false whenever stopage is needed *//
        while (running) {
            //* Within this try catch block we repeat the same API call that is within the .getCommandDigit() function.
            //* With that call we receive the commandDigit, either 0,1,2,3 and we only set it on the model whenever the value is different than 0.
            //* If we do not receive anything different than 0 we let it rest for 10 seconds and run it again. *//
            try {
            int commandDigit = boeBotController.getCommandDigit();
            if (commandDigit != 0) {
                boeBotController.setCommand(commandDigit);
            }
                    sleep(10000);
                    run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }
}
