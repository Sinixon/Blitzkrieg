package MVCBot;

import AStar.Node;
import CONFIG.CONFIG;
import HttpConnect.HttpConnect;
import TI.BoeBot;
import Threads.CommandThread;
import Threads.DiagnoseThread;
import Threads.DriveThread;
import json.Json;
import json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class APIBoeBotController {

    private APIBoeBotView boeBotView;
    private APIBoeBot boeBot;
    private HttpConnect httpConnect;
    private CommandThread commandThread;
    private DriveThread driveThread;
    private DiagnoseThread diagnoseThread;

    public APIBoeBotController(APIBoeBot boeBot, APIBoeBotView boeBotView, HttpConnect httpConnect) {
        this.boeBot = boeBot;
        this.boeBotView = boeBotView;
        this.httpConnect = httpConnect;
    }

    public String getId() {
        return boeBot.getId();
    }

    public String getAuthCode() {
        return boeBot.getAuthCode();
    }

    public void setXDigit(int xDigit) {
        boeBot.setXDigit(xDigit);
    }

    public int getXDigit() {
       return boeBot.getXDigit();
    }

    public void setYDigit(int yDigit) {
        boeBot.setYDigit(yDigit);
    }

    public int getYDigit() {
        return boeBot.getYDigit();
    }

    public void setStartValues(int[] startValues) {
        boeBot.setStartValues(startValues);
    }

    public int[] getStartValues() {
        return boeBot.getStartValues();
    }

    public void setEndValues(int[] endValues) {
        boeBot.setEndValues(endValues);
    }

    public int[] getEndValues() {
        return boeBot.getEndValues();
    }

    public void setBlockadeValues(ArrayList<String> blockadeValues) {
        boeBot.setBlockadeValues(blockadeValues);
    }

    public ArrayList<String> getBlockadeValues() {
        return boeBot.getBlockadeValues();
    }

    public void setPath(List<Node> path) {
        boeBot.setPath(path);
    }

    public List<Node> getPath() {
        return boeBot.getPath();
    }

    public DiagnoseThread getDiagnoseThread() {
        return boeBot.getDiagnoseThread();
    }

    public void setDiagnoseThread() {
        boeBot.setDiagnoseThread(diagnoseThread);
    }

    public DriveThread getDriveThread() {
        return boeBot.getDriveThread();
    }

    public void setDriveThread() {
        boeBot.setDriveThread(driveThread);
    }

    public CommandThread getCommandThread() {
        return boeBot.getCommandThread();
    }

    public void setCommandThread() {
        boeBot.setCommandThread(commandThread);
    }

    public void setLocation(String location) {
        boeBot.getHttpConnect().executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LocationSet", "{\"ID\" : \""+boeBot.getId()+"\",\"location\" : \""+location+"\"}", "POST");
    }

    public void setHttpConnect(HttpConnect httpConnect) {
        boeBot.setHttpConnect(httpConnect);
    }

    public HttpConnect getHttpConnect() {
        return boeBot.getHttpConnect();
    }


    public int getCommand() {
        return boeBot.getCommand();
    }

    public void setCommand(int command) {
         boeBot.setCommand(command);
    }

    public void resetCommand() {
        boeBot.getHttpConnect().executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/command/commandSet", "{\"ID\" : \""+boeBot.getId()+"\",\"API_code\" : \""+boeBot.getAuthCode()+"\"}", "GET");
    }

    //* Commando thread stuff *//


    public void startCommandThreadWithBoolean() {
        resetCommand();
        boeBot.getCommandThread().running = true;
//        boeBot.getCommandThread().run();
    }

    public void interruptDrive() {
        boeBot.getDriveThread().interruptDriveThread();
    }

    public void startDriveThreadFromBeginning() {
        boeBot.getDriveThread().start();
    }

    public void runDriveThread() {
        boeBot.getDriveThread().start();
    }

    public void startCommandThread() {
        resetCommand();
        boeBot.getCommandThread().start();
    }

    public void startDiagnoseThreadFromBeginning() {
        boeBot.getDiagnoseThread().start();
    }

    public void startDiagnoseThreadWithBoolean() {
        boeBot.getDiagnoseThread().running = true;
        boeBot.getDiagnoseThread().start();
    }

    public void runDiagnoseThread() {
        boeBot.getDiagnoseThread().running = true;
        boeBot.getDiagnoseThread().run();
    }


    public void startDriveThreadWithBoolean() {
        boeBot.getDriveThread().running = true;
//        boeBot.getDriveThread().start();
    }

    public void interruptDriveThread() {
        boeBot.getDriveThread().running = false;
    }

    public void interruptDiagnoseThread() {
        System.out.println("WORDT DE DRIVE THREAD GESTOPT?");
        boeBot.getDiagnoseThread().running = false;
    }

    public void interruptCommandThread() {
        boeBot.getCommandThread().running = false;
    }

    public void ledAtStart() {
        BoeBot.digitalWrite(CONFIG.LEDRIGHT, false);
        BoeBot.digitalWrite(CONFIG.LEDLEFT, false);
    }

    public int getCommandDigit() {
        JsonObject jsonObject = Json.parse(getHttpConnect().executeRequestMethod("https://bp6.adainforma.tk/blitzkrieg/api/command/Read.php", "{\"ID\" : \""+getId()+"\",\"API_code\" : \""+getAuthCode()+"\"}", "GET")).asObject();
        int commandDigit = Integer.parseInt(jsonObject.getString("command", "0"));
        return commandDigit;
    }
}
