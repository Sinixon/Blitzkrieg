package Threads;

import CONFIG.CONFIG;
import HttpConnect.HttpConnect;
import MVCBot.APIBoeBotController;
import TI.BoeBot;
import TI.Servo;


import static Threads.DriveThread.*;
import static java.lang.Thread.sleep;

public class DiagnoseThread implements Runnable {

    private APIBoeBotController boeBotController;
    private HttpConnect httpConnect;

    private Thread worker;

    public boolean running = true;

    //CONFIG of the infrared sensors
    static int AnalogPin1 = CONFIG.LSLEFT;  // left
    static int AnalogPin2 = CONFIG.LSMIDDLE;  // Middle
    static int AnalogPin3 = CONFIG.LSRIGHT;  // right

    //CONFIG of servo
    static Servo servoLeft = new Servo(CONFIG.SERVOLEFT);
    static Servo servoRight = new Servo(CONFIG.SERVORIGHT);

    public DiagnoseThread(APIBoeBotController boeBotController, HttpConnect httpConnect) {
        this.boeBotController = boeBotController;
        this.httpConnect = httpConnect;
    }

    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    @Override
    public void run() {
        //* Same principle with the while (running) loop as within the CommandThread
        //* The difference in this thread is that it uses the getCommand() function to receive the command from the model.
        //* If the received command is 3 then we go into the startDiagnose() function. Else sleep for 15 seconds *//
        while (running) {
            if (boeBotController.getCommand() == 3) {
                startDiagnose();
            }
            try {
                sleep(15000);
                run();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    //* Whenver the command is 3 we go into this function. This function stops the running CommandThread and DriveThread so that only the DiagnoseThread is running. *//
    public void startDiagnose() {
        boeBotController.interruptCommandThread();
        boeBotController.interruptDriveThread();
        //* Update the log *//
        httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.DIAGNOSESTART+"\"}", "POST");
        //* Start the LED diagnose *//
        legDiagnose();
    }

    public void legDiagnose() {
        //* Leds on for 10 seconds
        BoeBot.digitalWrite(CONFIG.LEDRIGHT, true);
        BoeBot.digitalWrite(CONFIG.LEDLEFT, true);
        BoeBot.wait(10000);
        BoeBot.digitalWrite(CONFIG.LEDRIGHT, false);
        BoeBot.digitalWrite(CONFIG.LEDLEFT, false);
        BoeBot.wait(5000);
        //* Move on to the ultraSonicDiagnose() function *//
        ultraSonicDiagnose();
    }

    public void ultraSonicDiagnose() {
        //* While true loop to receive the distance of an object *//
        while(true) {
            BoeBot.digitalWrite(CONFIG.USTRIGGER, true);
            BoeBot.wait(10);
            BoeBot.digitalWrite(CONFIG.USTRIGGER, false);

            int i = BoeBot.pulseIn(CONFIG.USECHO, true, 10000);
            System.out.println("Distance of object: " + i / 58);
            //* Whenever the distance of the object is smaller than 20 we know it works correctly. Let the LED's blink so the user knows it works. *//
            if (i / 58 < 20) {
                ledBlink(0);
                break;
            }
        }
        BoeBot.wait(5000);
        //* Start the servoMotorDiagnose() function *//
        servoMotorDiagnose();
    }

    public void servoMotorDiagnose() {
        //* Turn on the right Servo for 5 seconds and after that the left Servo. *//
        servoRight.update(1500 + 19);
        BoeBot.wait(5000);
        servoRight.update(1500);
        BoeBot.wait(5000);
        servoLeft.update(1500 - 20);
        BoeBot.wait(5000);
        servoLeft.update(1500);
        BoeBot.wait(5000);
        //* Start the infraRedDiagnose() function *//
        infraRedDiagnose();
    }


    public void infraRedDiagnose() {
        //* While true loop the keep reading the values of the infraRedSensors. If all three of them exceed the sensitivty value of 150 let the LED's blink. *//
        while(true) {
            int sensor1 = BoeBot.analogRead(AnalogPin1);
            int sensor2 = BoeBot.analogRead(AnalogPin2);
            int sensor3 = BoeBot.analogRead(AnalogPin3);
            System.out.println("Value of right infrared sensor: " + BoeBot.analogRead(CONFIG.LSRIGHT));
            System.out.println("Value of middle infrared sensor: " + BoeBot.analogRead(CONFIG.LSMIDDLE));
            System.out.println("Value of left infrared sensor: " + BoeBot.analogRead(CONFIG.LSLEFT));
            if ((sensor1 > CONFIG.SENSITIVITY) && (sensor2 > CONFIG.SENSITIVITY) && (sensor3 > CONFIG.SENSITIVITY)) {
                ledBlink(0);
                break;
            }
        }
        BoeBot.wait(5000);
        //* Start the speaker diagnose function *//
        speakerDiagnose();
    }

    public void speakerDiagnose() {
        //* Let the speaker make a 1000 frequencty sound for 5 seconds. *//
        BoeBot.freqOut(CONFIG.FREQUENTY, 1000, 5000);
        BoeBot.wait(5000);
        //* Stop the diagnose function *//
        stopDiagnose();
    }

    public void stopDiagnose() {
        //* Log the completed diagnose. Then start up the command and drive thread classes again so they can they for a command.
        //* Also stop the diagnose thread and start it back up again. *//
        httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.DIAGNOSESTOP+"\"}", "POST");
        boeBotController.startCommandThreadWithBoolean();
        boeBotController.runDriveThread();
        boeBotController.interruptDiagnoseThread();
    }

    public void ledBlink(int x) {
        if (x < 100) {
            BoeBot.digitalWrite(CONFIG.LEDRIGHT, true);
            BoeBot.digitalWrite(CONFIG.LEDLEFT, true);
            BoeBot.wait(50);
            BoeBot.digitalWrite(CONFIG.LEDRIGHT, false);
            BoeBot.digitalWrite(CONFIG.LEDLEFT, false);
            BoeBot.wait(50);
            x++;
            ledBlink(x);
        }
    }
}


