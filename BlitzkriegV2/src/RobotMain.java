import CONFIG.CONFIG;
import MVCBot.APIBoeBot;
import MVCBot.APIBoeBotController;
import MVCBot.APIBoeBotView;
import Threads.CommandThread;
import HttpConnect.HttpConnect;
import Threads.DiagnoseThread;
import Threads.DriveThread;
import json.Json;
import json.JsonObject;

public class RobotMain {

    public static void main(String[] args) {

        HttpConnect httpConnect = new HttpConnect();

        //* Create a model of the boebot *//
        APIBoeBot boeBot = retriveBoeBot(CONFIG.USERNAME, CONFIG.PASSWORD, httpConnect);

        //* Link the created object of HttpConnect to the boeBot model
        // This way we can use it in any class which contains the controller *//
        boeBot.setHttpConnect(httpConnect);

        //* Create a view *//
        APIBoeBotView boeBotView = new APIBoeBotView();

        //* Create a controller and send the view and model with the controller and the httpConnect *//
        APIBoeBotController boeBotController = new APIBoeBotController(boeBot, boeBotView, httpConnect);

        //* Create drive thread
        DriveThread driveThread = new DriveThread(boeBotController, httpConnect);

        //* Create a diagnose thread
        DiagnoseThread diagnoseThread = new DiagnoseThread(boeBotController, httpConnect);

        // Create a command thread
        CommandThread commandThread = new CommandThread(boeBotController, httpConnect);

        //* Set all of the threads to the model. This way the controller can receive them through the model *//
        boeBot.setCommandThread(commandThread);
        boeBot.setDiagnoseThread(diagnoseThread);
        boeBot.setDriveThread(driveThread);

        //* Set LED off at start *//
        boeBotController.ledAtStart();

        //* Start all of the threads *//
        boeBotController.startCommandThread();
        boeBotController.startDriveThreadFromBeginning();
        boeBotController.startDiagnoseThreadFromBeginning();
    }

    private static APIBoeBot retriveBoeBot(String username, String password, HttpConnect httpConnect){
        //* Create a boebot model object *//
        APIBoeBot boeBot = new APIBoeBot();

        //* Set auth code and id on the model *//
        JsonObject jsonObject = Json.parse(httpConnect.executeRequestMethod("https://bp6.adainforma.tk/blitzkrieg/api/auth/AuthSet", "{\"username\" : \""+username+"\",\"password\" : \""+password+"\"}", "POST")).asObject();
        boeBot.setAuthCode(jsonObject.getString("API_code", "No API Code"));
        boeBot.setId(jsonObject.getString("ID", "No ID"));

        //* Set location to zero to make the bot come online *//
        httpConnect.executeRequestMethodNoResponseRead("https://bp6.adainforma.tk/blitzkrieg/api/log/LocationSet", "{\"ID\" : \""+boeBot.getId()+"\",\"API_code\" : \""+boeBot.getAuthCode()+"\",\"location\" : \""+CONFIG.RESETLOCATION +"\"}", "POST");
        return boeBot;
    }
}

