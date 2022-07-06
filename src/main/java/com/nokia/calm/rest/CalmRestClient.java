/*
 * Nokia Software 2020
 * Created at: 27-Sep-2021 00:23:13
 * Created by: Caglar Kilincoglu
 */
package com.nokia.calm.rest;

import com.comptel.mc.node.logging.NodeLoggerFactory;
import com.comptel.mc.node.logging.TxeLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nokia.calm.model.Alarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class CalmRestClient {

    private static final TxeLogger LOGGER = NodeLoggerFactory.getNodeLogger(CalmRestClient.class.getCanonicalName());

    private final String CALM_HOST;
    private final int CALM_PORT;

    private final String  GET_ALL_ACTIVE_ALARMS;

    private final String CLEAR_ACTIVE_ALARM;
    private final static Type CALM_ALARM_LIST_TYPE = new TypeToken<ArrayList<Alarm>>() {
    }.getType();

    public CalmRestClient(String calmHost, int calmPort) {
        CALM_HOST = calmHost;

        CALM_PORT = calmPort;

        GET_ALL_ACTIVE_ALARMS = "http://" + CALM_HOST + ":" + CALM_PORT + "/api/alma/alarms/";
        
        CLEAR_ACTIVE_ALARM = "http://" + CALM_HOST + ":" + CALM_PORT + "/api/alma/alarms/%d/clear";
    }


    /**
     * Fetches all active alarms from CALM
     *
     * @return list of Alarm objects
     * @throws IOException
     */
    public List<Alarm> getAllActiveAlarms() throws IOException {
        LOGGER.info("getAllActiveAlarms(): enter");
        return transformNonFilteredJsonResponse(sendRestGetRequest(GET_ALL_ACTIVE_ALARMS));

    }

    private String sendRestGetRequest(String urlinput) throws MalformedURLException, IOException {
        LOGGER.info("sendRestGetRequest(): start");
        LOGGER.info("sendRestGetRequest(): URL = " + urlinput);
        URL url = new URL(urlinput);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                                       + conn.getResponseCode());
        }
        LOGGER.info("sendRestGetRequest(): Return response code " + conn.getResponseCode());
        BufferedReader br = null;
        String output;
        String appended = "";
        try {

            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            while ((output = br.readLine()) != null) {
                LOGGER.info("sendRestGetRequest(): Current line: " + output);
                appended = appended + output;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        conn.disconnect();

        LOGGER.info("sendRestGetRequest(): ServerResponse: " + appended);
        return appended;
    }

    public void clearAlarm(String ackUser, long alarmId) throws IOException {
        LOGGER.info("clearAlarm(): start");
        Map<String, String> postParam = new HashMap<>();
        postParam.put("ackUserId", ackUser);
        
        String urlinput = String.format(CLEAR_ACTIVE_ALARM, alarmId);
        LOGGER.info("clearAlarm(): urlinput: " + urlinput);
        sendRestPostRequest(urlinput, postParam);
        
        LOGGER.info("clearAlarm(): end");

    }

    private void sendRestPostRequest(String urlinput, Map<String, String> paramList) throws MalformedURLException, IOException {
        LOGGER.info("sendRestPostRequest(): start");
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : paramList.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        LOGGER.info("URL = " + urlinput);
        URL url = new URL(urlinput);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        if (conn.getResponseCode() != 204) {
            throw new RuntimeException("Failed : HTTP error code : "
                                       + conn.getResponseCode());
        }

        conn.disconnect();

        LOGGER.info("sendRestPostRequest(): end");
    }

    private List<Alarm> transformNonFilteredJsonResponse(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, CALM_ALARM_LIST_TYPE);
    }

}
