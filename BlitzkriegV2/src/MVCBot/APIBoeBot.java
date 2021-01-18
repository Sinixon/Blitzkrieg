package MVCBot;

import AStar.Node;
import HttpConnect.HttpConnect;
import Threads.CommandThread;
import Threads.DiagnoseThread;
import Threads.DriveThread;

import java.util.ArrayList;
import java.util.List;

public class APIBoeBot {

    private String authCode;
    private String id;
    private int xDigit;
    private int yDigit;
    private int[] startValues;
    private int[] endValues;
    private ArrayList<String> blockadeValues;
    private List<Node> path;
    private HttpConnect httpConnect;
    private int command;
    private DiagnoseThread diagnoseThread;
    private DriveThread driveThread;
    private CommandThread commandThread;

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getXDigit() {
        return xDigit;
    }

    public void setXDigit(int xDigit) {
        this.xDigit = xDigit;
    }

    public int getYDigit() {
        return yDigit;
    }

    public void setYDigit(int yDigit) {
        this.yDigit = yDigit;
    }

    public int[] getStartValues() {
        return startValues;
    }

    public void setStartValues(int[] startValues) {
        this.startValues = startValues;
    }

    public int[] getEndValues() {
        return endValues;
    }

    public void setEndValues(int[] endValues) {
        this.endValues = endValues;
    }

    public ArrayList<String> getBlockadeValues() {
        return blockadeValues;
    }

    public void setBlockadeValues(ArrayList<String> blockadeValues) {
        this.blockadeValues = blockadeValues;
    }

    public List<Node> getPath() {
        return path;
    }

    public void setPath(List<Node> path) {
        this.path = path;
    }

    public void setHttpConnect(HttpConnect httpConnect) {
        this.httpConnect = httpConnect;
    }

    public HttpConnect getHttpConnect() {
        return httpConnect;
    }


    public void setCommand(int command) {
        this.command = command;
    }

    public int getCommand() {
        return command;
    }



    public void setDiagnoseThread(DiagnoseThread diagnoseThread) {
        this.diagnoseThread = diagnoseThread;
    }

    public DiagnoseThread getDiagnoseThread() {
        return diagnoseThread;
    }

    public void setDriveThread(DriveThread driveThread) {
        this.driveThread = driveThread;
    }

    public DriveThread getDriveThread() {
        return driveThread;
    }

    public void setCommandThread(CommandThread commandThread) {
        this.commandThread = commandThread;
    }

    public CommandThread getCommandThread() {
        return commandThread;
    }

}
