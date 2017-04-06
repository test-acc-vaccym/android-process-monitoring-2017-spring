package com.processmonitoring.request_handler;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.processmonitoring.bean.CcsOutgoingMessage;
import com.processmonitoring.server.MessageHelper;
import com.processmonitoring.server.XMPPServer;
import com.processmonitoring.util.Util;
/**
 * Handles request from android and desktop client
 */
public class RequestHandler {
	private static XMPPServer xmppServer;
	public static void setServer(XMPPServer xmppServer) {
		RequestHandler.xmppServer = xmppServer;
	}
	
	public static void handleRequest(String data) {
		try {
			Map<String, Object> listData = (Map<String, Object>) JSONValue.parseWithException(data);
			String type = (String) listData.get("requestType");
			switch(type) {
				case "registration":
					System.out.println("LOGIN: " + listData.get("login"));
					System.out.println("PASSWORD: " + listData.get("password"));
					break;
				case "get-list-apps":
					JSONObject jsonListApps = new JSONObject();
					int k = 0;
					 for (Map.Entry<String, Object> entry : listData.entrySet()) {
						 if(!entry.getKey().equals("requestType")) {
							 if(k < 10) {
					    		 jsonListApps.put(entry.getKey(), entry.getValue()); 
						    } else {
						    	jsonListApps.put(entry.getKey(), entry.getValue()); 
						    	RequestHandler.sendResponseToDevice("list-apps", jsonListApps.toString());
						    	jsonListApps.clear();
						    	k = 0;
						    }
						    k++;
						 }  
					 }	
					 //Send back list of apps to android device
					RequestHandler.sendResponseToDevice("list-apps", jsonListApps.toString());
					 break;
				case "update-blacklist":
					JSONObject jsonBlacklist = new JSONObject();
					 for (Map.Entry<String, Object> entry : listData.entrySet()) {
						  if(!entry.getKey().equals("requestType")) {
							  System.out.println("BLOCKED APP: Package: " + entry.getKey() + " Name: " + entry.getValue());
							  jsonBlacklist.put(entry.getKey(), entry.getValue()); 
						 }
						   
					 }	
					 //Send back list of apps to android device
					 //SQLite database on device will be updated
					RequestHandler.sendResponseToDevice("update-blacklist", jsonBlacklist.toString());
						
					break;
				default:
					break;
			}
		} catch(ParseException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Sends data to device
	 * it can be request or updated list of applications
	 */
	 public static void sendResponseToDevice(String requestType, String data) {
		    String messageId = Util.getUniqueMessageId();
	 		Map<String, String> dataPayload = new HashMap<String, String>();
	 		dataPayload.put("requestType", requestType);
	 		dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, data);
	 		CcsOutgoingMessage message = new CcsOutgoingMessage(Util.Device_token, messageId, dataPayload);
	 		String jsonRequest = MessageHelper.createJsonOutMessage(message);
	 		xmppServer.send(jsonRequest);
	}
}
