package CONFIG;

public class CONFIG {

    //configuration of web app settings
    public static final String USERNAME = "bot1";
    public static final String PASSWORD = "bot1";

    //configuration of start view
    public static String compas = "NORTH";

    //configuration of the ports used
    //infrared sensor
    public static final int LSRIGHT = 2;
    public static final int LSMIDDLE = 1;
    public static final int LSLEFT = 0;
    //servor
    public static final int SERVOLEFT = 13;
    public static final int SERVORIGHT = 12;
    // led's
    public static final int LEDLEFT = 3;
    public static final int LEDRIGHT = 2;
    //speaker
    public static final int FREQUENTY = 1;
    //ultrasonic sensor
    public static final int USTRIGGER = 4;
    public static final int USECHO = 5;

    //configuration of speed settings
    public static final int SPEEDLEFTSERVO = 1500;
    public static final int SPEEDRIGHTSERVO = 1500;
    public static final int SPEED = 20;
    public static final int SENSITIVITY = 150;

    //configuration of start location
    public static final String RESETLOCATION = "0.0";

    //CONFIG of lijnzoeker
    static int AnalogPin1 = CONFIG.LSLEFT;  // left
    static int AnalogPin2 = CONFIG.LSMIDDLE;  // Middle
    static int AnalogPin3 = CONFIG.LSRIGHT;  // right

    //CONFIG of log settings
    public static final String START = "Starting route";
    public static final String TURNLEFT = "Making a left turn";
    public static final String TURNRIGHT = "Making a right turn";
    public static final String FORWARD = "Going straight ahead";
    public static final String BLOCKADE = "Detected a new blockade";
    public static final String DIAGNOSESTART = "Starting diagnose";
    public static final String DIAGNOSESTOP = "Completed diagnose";
    public static final String STOP = "Reached the end. Stopping";

}

