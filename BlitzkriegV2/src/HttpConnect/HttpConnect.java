package HttpConnect;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnect {

    URL url;

    public String executeRequestMethod(String targetURL, String urlParameters, String requestMethod) {

        HttpURLConnection connection = null;

        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept","*/*");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            int responseCode = connection.getResponseCode();
            InputStream inputStream = null;
            if (responseCode == 200) {
                inputStream = new BufferedInputStream(connection.getInputStream());
            } else {
                inputStream = new BufferedInputStream(connection.getErrorStream());
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            inputStream.close();
            connection.disconnect();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void executeRequestMethodNoResponseRead(String targetURL, String urlParameters, String requestMethod) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept","*/*");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            int responseCode = connection.getResponseCode();
            InputStream inputStream = null;
            if (responseCode == 200) {
                inputStream = new BufferedInputStream(connection.getInputStream());
            } else {
                inputStream = new BufferedInputStream(connection.getErrorStream());
            }
            inputStream.close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
