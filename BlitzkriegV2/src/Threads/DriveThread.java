package Threads;

import AStar.AStar;
import AStar.Node;
import CONFIG.CONFIG;
import HttpConnect.HttpConnect;
import MVCBot.APIBoeBotController;
import TI.BoeBot;
import TI.Servo;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import jdk.nashorn.api.scripting.ScriptUtils;
import json.Json;
import json.JsonArray;
import json.JsonObject;
import json.JsonValue;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class DriveThread implements Runnable {

    //Counters we will use later
    int counterOld = 0;
    int counterNew = 1;

    private APIBoeBotController boeBotController;
    private HttpConnect httpConnect;
    public boolean running = true;
    private Thread worker;
    private int ultraSonicSensor;

    //CONFIG of servo
    static Servo servoLeft = new Servo(CONFIG.SERVOLEFT);
    static Servo servoRight = new Servo(CONFIG.SERVORIGHT);

    //CONFIG of infraRedSensors
    static int AnalogPin1 = CONFIG.LSLEFT;  // Left
    static int AnalogPin2 = CONFIG.LSMIDDLE;  // Middle
    static int AnalogPin3 = CONFIG.LSRIGHT;  // Right

    public DriveThread(APIBoeBotController boeBotController, HttpConnect httpConnect) {
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
        //* If the received command is 1 then we go into the startRoute() function. Else sleep for 15 seconds *//
        while (running) {
            if (boeBotController.getCommand() == 1) {
                startRoute();
            } else {
                try {
                    sleep(15000);
                    run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    //--------------------------------->>>>>>STOP ROUTE REGION<<<<<<---------------------------------\\

    public void stopRoute() {
        servoLeft.update(1500);
        servoRight.update(1500);
        httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.STOP+"\"}", "POST");
        boeBotController.startCommandThreadWithBoolean();
        boeBotController.startDiagnoseThreadWithBoolean();
        boeBotController.interruptDriveThread();
    }

    //--------------------------------->>>>>>START ROUTE REGION<<<<<<---------------------------------\\

    public void startRoute() {
        //* First stop diagnoseThread and the commandThread
        //* Then set all of the route information with function setRouteInformation(). *//
        boeBotController.interruptCommandThread();
        boeBotController.interruptDiagnoseThread();
        setRouteInformation();
        httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.START+"\"}", "POST");
        System.out.println(boeBotController.getPath().toString());
        boeBotController.getPath().remove(0); //remove the first value of the path to drive
        //* Start driving with the drive function. *//
        drive(CONFIG.compas, "");
    }

    //--------------------------------->>>>>>DRIVE ROUTE REGION<<<<<<---------------------------------\\

    public void drive(String compas, String decision) {
        while (true) {
            int sensor1 = BoeBot.analogRead(AnalogPin1);
            int sensor2 = BoeBot.analogRead(AnalogPin2);
            int sensor3 = BoeBot.analogRead(AnalogPin3);
            BoeBot.digitalWrite(CONFIG.USTRIGGER, false);
            BoeBot.wait(50);
            BoeBot.digitalWrite(CONFIG.USTRIGGER, true);
            BoeBot.wait(50);
            BoeBot.digitalWrite(CONFIG.USTRIGGER, false);
            ultraSonicSensor = BoeBot.pulseIn(CONFIG.USECHO, true, 5000);

            if ((sensor1 < CONFIG.SENSITIVITY) && (sensor3 < CONFIG.SENSITIVITY) && (sensor2 > CONFIG.SENSITIVITY)) {
                System.out.println("Distance of object: " + ultraSonicSensor / 58);
                //* Whenever an object is found then stop driving and calculate the route again. *//
                if (ultraSonicSensor / 58 < 20) {
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"API_code\" : \""+boeBotController.getAuthCode()+"\",\"log\" : \""+CONFIG.BLOCKADE+"\"}", "POST");
                    servoLeft.update(1500);
                    servoRight.update(1500);
                    BoeBot.freqOut(CONFIG.FREQUENTY, 1000, 5000);

                    servoLeft.update(1500);
                    servoRight.update(1500);

                    ArrayList<String> blockadeValues = boeBotController.getBlockadeValues();
                    String blockadeToAdd = boeBotController.getPath().get(counterNew).toString();

                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/route/BlockadeSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"API_code\" : \""+boeBotController.getAuthCode()+"\",\"blockade\" : \""+blockadeToAdd+"\"}", "POST");
                    blockadeValues.add(blockadeValues.size(), blockadeToAdd);

                    boeBotController.setBlockadeValues(blockadeValues);

                    int[] startValues = {boeBotController.getPath().get(counterOld).getRow() - 1,boeBotController.getPath().get(counterOld).getCol() - 1};

                    boeBotController.setStartValues(startValues);

                    List<Node> path = getNodes(boeBotController.getXDigit(), boeBotController.getYDigit(), boeBotController.getStartValues(), boeBotController.getEndValues(), boeBotController.getBlockadeValues());

                    boeBotController.setPath(path);
                    counterOld = 0;
                    counterNew = 1;
                    drive("NORTH", "");
                }
                //naar voren
                servoRight.update(1500 + 19);
                servoLeft.update(1500 - 20);

            }
            else if (((sensor1 > CONFIG.SENSITIVITY) && (sensor2 > CONFIG.SENSITIVITY) && (sensor3 < CONFIG.SENSITIVITY)) || ((sensor1 > CONFIG.SENSITIVITY) && (sensor2 < CONFIG.SENSITIVITY) && (sensor3 < CONFIG.SENSITIVITY))) {
                //* Correction to the right *//
                BoeBot.wait(100);
                servoRight.update(1500);
                servoLeft.update(1500);

                servoRight.update(1500 - 5);
                servoLeft.update(1500);
            } else if (((sensor1 < CONFIG.SENSITIVITY) && (sensor2 > CONFIG.SENSITIVITY) && (sensor3 > CONFIG.SENSITIVITY)) || ((sensor1 < CONFIG.SENSITIVITY) && (sensor2 < CONFIG.SENSITIVITY) && (sensor3 > CONFIG.SENSITIVITY))) {
                //* Correction to the left *//
                BoeBot.wait(100);
                servoRight.update(1500);
                servoLeft.update(1500);

                servoRight.update(1500);
                servoLeft.update(1500 + 5);
            } else if ((sensor1 >= CONFIG.SENSITIVITY) && (sensor2 >= CONFIG.SENSITIVITY) && (sensor3 >= CONFIG.SENSITIVITY)) {
                //* Update live location through setLocation() function. We send the node we just hit. *//
                boeBotController.setLocation(boeBotController.getPath().get(counterOld).toString());

                //* If the counterNew is smaller then the path there are still nodes to drive. If not we stop *//
                if (counterNew <= boeBotController.getPath().size() - 1) {
                    //* Check for both column and rows if it changed, if it did, check if the col/row went up (>) or down (<). *//
                    //* Then send a decision string with whatever it did and execute the function directionToDrive with that decision. *//
                    if ((boeBotController.getPath().get(counterOld).getCol() != boeBotController.getPath().get(counterNew).getCol())) {
                        if ((boeBotController.getPath().get(counterNew).getCol() > boeBotController.getPath().get(counterOld).getCol())) {
                                decision = "NORTH";
                                counterOld++;
                                counterNew++;
                                directionToDrive(compas, decision);
                            }
                        else if ((boeBotController.getPath().get(counterNew).getCol() < boeBotController.getPath().get(counterOld).getCol())) {
                            decision = "EAST";
                            counterOld++;
                            counterNew++;
                            directionToDrive(compas, decision);
                        }
                    } else if ((boeBotController.getPath().get(counterOld).getRow() != boeBotController.getPath().get(counterNew).getRow())) {
                       if ((boeBotController.getPath().get(counterNew).getRow() > boeBotController.getPath().get(counterOld).getRow())) {
                           decision = "WEST";
                           counterNew++;
                           counterOld++;
                           directionToDrive(compas, decision);
                        } else if ((boeBotController.getPath().get(counterNew).getRow() < boeBotController.getPath().get(counterOld).getRow())) {
                           decision = "EAST";
                           counterNew++;
                           counterOld++;
                           directionToDrive(compas, decision);
                       }
                    }
                } else {
                    //* Stop if the counterNew exceeds the path size - 1. *//
                    stopRoute();
                    break;
                }
                break;
            }
        }
    }

    public void directionToDrive(String compas, String decision) {
        //* This switchs checks what the decision is and makes a choice which direction to drive based on the current compas. *//
        switch (decision) {
            case "NORTH":
                if (compas == "NORTH") {
                    straightAhead();
                    compas = decision;
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.FORWARD+"\"}", "POST");
                    drive(compas, decision);
                } else if (compas == "WEST") {
                    compas = decision;
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.TURNRIGHT+"\"}", "POST");
                    rightTurn(compas, decision);
                } else if (compas == "EAST") {
                    compas = decision;
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.TURNLEFT+"\"}", "POST");
                    leftTurn(compas, decision);
                }
                compas = decision;
                break;
            case "WEST":
                if (compas == "WEST") {
                    straightAhead();
                    compas = decision;
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.FORWARD+"\"}", "POST");
                    drive(compas, decision);
                } else if (compas == "NORTH") {
                    compas = decision;
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.TURNLEFT+"\"}", "POST");
                    leftTurn(compas, decision);
                }
                compas = decision;
                break;
            case "EAST":
                if(compas == "EAST") {
                    straightAhead();
                    compas = decision;
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.FORWARD+"\"}", "POST");
                    drive(compas, decision);
                } else if (compas == "NORTH") {
                    compas = decision;
                    rightTurn(compas, decision);
                    httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LogSet", "{\"ID\" : \""+boeBotController.getId()+"\",\"log\" : \""+CONFIG.TURNRIGHT+"\"}", "POST");
                    drive(compas, decision);
                }
        }
    }

    public void straightAhead() {
        servoRight.update(1500 + 19);
        servoLeft.update(1500 - 20);
        BoeBot.wait(3000);
    }

    public void leftTurn(String compas, String decision) {
        servoRight.update(1500 + 19);
        servoLeft.update(1500 - 20);
        BoeBot.wait(300);

        servoRight.update(1500);
        servoLeft.update(1500 - 25);

        turnOnLeftLED(true);
        drive(compas, decision);
    }

    public void rightTurn(String compas, String decision) {
        servoRight.update(1500 + 19);
        servoLeft.update(1500 - 20);
        BoeBot.wait(300);

        servoLeft.update(1500);
        servoRight.update(1500 + 25);
        turnOnRightLED(true);

        drive(compas, decision);
    }

    public void turnOnRightLED(boolean state) {
        BoeBot.digitalWrite(CONFIG.LEDRIGHT, state);
        BoeBot.wait(3000);
        state = !state;
        BoeBot.digitalWrite(CONFIG.LEDRIGHT, state);
    }


    public void turnOnLeftLED(boolean state) {
        BoeBot.digitalWrite(CONFIG.LEDLEFT, state);
        BoeBot.wait(3300);
        state = !state;
        BoeBot.digitalWrite(CONFIG.LEDLEFT, state);
    }

    //--------------------------------->>>>>>INITIATE ROUTE REGION<<<<<<---------------------------------\\

    public void setRouteInformation() {
        JsonObject jsonRoute = Json.parse(httpConnect.executeRequestMethod("https://bp6.adainforma.tk/blitzkrieg/api/route/Read", "{\"ID\" : \""+boeBotController.getId()+"\",\"API_code\" : \""+boeBotController.getAuthCode()+"\"}", "GET")).asObject();

        //* Set X value *//
        boeBotController.setXDigit(Integer.parseInt(jsonRoute.get("route").asArray().get(0).asObject().get("MAX_X").asString()));

        //* Set Y value *//
        boeBotController.setYDigit(Integer.parseInt(jsonRoute.get("route").asArray().get(0).asObject().get("MAX_Y").asString()));

        //* Set start coordinates *//
        int[] startValues = {Integer.parseInt(jsonRoute.get("route").asArray().get(0).asObject().get("start").asString().substring(0, 1)), Integer.parseInt(jsonRoute.get("route").asArray().get(0).asObject().get("start").asString().substring(jsonRoute.get("route").asArray().get(0).asObject().get("start").asString().length() - 1))};
        boeBotController.setStartValues(startValues);

        //* Set end coordinates *//
        int[] endValues = {Integer.parseInt(jsonRoute.get("route").asArray().get(0).asObject().get("end").asString().substring(0, 1)), Integer.parseInt(jsonRoute.get("route").asArray().get(0).asObject().get("end").asString().substring(jsonRoute.get("route").asArray().get(0).asObject().get("end").asString().length() - 1))};
        boeBotController.setEndValues(endValues);

        //* Set blockades *//
        JsonArray arrayRoute = jsonRoute.get("route").asArray();
        JsonValue value = arrayRoute.get(0).asObject().get("blockade");
        String blockadeValue = value.toString().replaceAll("^.|.$", "").replaceAll("[\\\\]", "");
        blockadeValue = "{\"blockade\": " + blockadeValue + "}";
        jsonRoute = Json.parse(blockadeValue).asObject();
        JsonArray arrayBlockade = jsonRoute.get("blockade").asArray();
        ArrayList<String> blockadeValues = new ArrayList<>();

        for (int i = 0; i < arrayBlockade.size(); i++) {
            blockadeValues.add(i, arrayBlockade.get(i).asString());
        }

        boeBotController.setBlockadeValues(blockadeValues);

        //* Set path *//
        List<Node> path = getNodes(boeBotController.getXDigit(), boeBotController.getYDigit(), boeBotController.getStartValues(), boeBotController.getEndValues(), boeBotController.getBlockadeValues());
        boeBotController.setPath(path);
    }

    public List<Node> getNodes(int xDigit, int yDigit, int[] startValues, int[] endValues, ArrayList<String> blockadeValues) {

        //initialnode en final node aanmaken met de parameters meegekregen. Arrays hebben altijd maar 2 waardes
        Node initialNode = new Node(startValues[1], startValues[0]);
        Node finalNode = new Node(endValues[1], endValues[0]);

        int xAs = yDigit;
        int yAs = xDigit;

        //Astar object aanmaken
        AStar aStar = new AStar(xAs + 10, yAs + 10, initialNode, finalNode);

        //dit blok is om 2 integers te initieren vanuit de array
        //deze twee integers zijn nodig om de blockade array te initieren
        String firstInt = blockadeValues.get(0).substring(0, 1);
        String secondInt = blockadeValues.get(0).substring(blockadeValues.get(0).length() - 1);

        //initieren van de blocksArray met [row][column]
        int[][] blocksArray = new int[][]{{Integer.parseInt(secondInt), Integer.parseInt(firstInt)}};
        //for loop aanspreken om door de array van blockadeValues te loopen
        //binnen deze loop wordt ook de functie getStringsFromArrayList aangesproken
        //als parameter wordt het item vanuit de arraylist meegegeven en de blocksArray[][]
        //Zo worden alle items uitgelezen en in de blocksArray[][] gezet
        for(int x = 1; x < blockadeValues.size(); x++) {
            String toTransfer = blockadeValues.get(x);
            blocksArray = getIntFromArrayList(toTransfer, blocksArray);
        }


        //blokkade array zetten op het aStar object
        aStar.setBlocks(blocksArray);


        //lijst van paden aanmaken met de findPath functie
        List<Node> path = aStar.findPath();
        //start de route met de nodes
        return path;
    }

    public int[][] getIntFromArrayList(String toTransfer, int arr[][]) {
        int x = Integer.parseInt(toTransfer.substring(0, 1));
        int z = Integer.parseInt(toTransfer.substring(toTransfer.length() - 1));
        int newArr[][] = addX(arr.length, arr, x, z);
        return newArr;
    }

    // Function to add x in arr
    public static int[][] addX(int n, int arr[][], int x, int z) {
        int i;

        // create a new array of size n+1
        int newarr[][] = new int[n + 1][n + 1];

        // insert the elements from
        // the old array into the new array
        // insert all elements till n
        // then insert x at n+1
        for (i = 0; i < n; i++) {
            newarr[i][0] = arr[i][0];
            newarr[i][1] = arr[i][1];
        }
        newarr[n][0] = z;
        newarr[n][1] = x;

        return newarr;
    }
}


