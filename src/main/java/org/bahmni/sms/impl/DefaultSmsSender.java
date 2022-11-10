package org.bahmni.sms.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bahmni.sms.SMSSender;
import org.bahmni.sms.model.Message;
import org.bahmni.sms.model.SMSRequest;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DefaultSmsSender implements SMSSender {

    public static final String CLIENT_ID = "MOK3WVSPLJIC128rjvvOAQ8Six0zZRgPaUrnRLc0";
    public static final String CLIENT_SECRET = "VAF63B9r9cjn3CQI7S3j0NBrTcB21WEWFgywa2t24BjAVntLmXjanaBr1Pt450v5Sma4lGpjMWZ1wcBQVIvP8BBwt4gRRgUBAmb1XDfFDl0PagL9Ak6mdEeGdPaTRLt1";
    public static final String SENDER = "Bahmni";

    private String getAuthorization() {
        try {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("https://api.d7networks.com/auth/v1/login/application");
        request.addHeader("mime-type", "multipart/form-data");
        List<NameValuePair> urlParameters = new ArrayList<>();

        urlParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
        urlParameters.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = httpClient.execute(request);
        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
        return jsonObject.get("token_type") + " " + jsonObject.get("access_token");
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error " + e;
        }
    }

    @Override
    public String send(String phoneNumber, String messageText) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost("https://api.d7networks.com/messages/v1/send");
            Message message = new Message();
            message.setChannel("sms");
            message.setMsg_type("text");
            message.setRecipients(new ArrayList<String>() {{
                add(phoneNumber);
            }});
            message.setOriginator(SENDER);
            message.setContent(messageText);
            SMSRequest smsRequest = new SMSRequest();
            smsRequest.setMessages(new ArrayList<Message>() {{
                add(message);
            }});
            ObjectMapper Obj = new ObjectMapper();
            String jsonObject = Obj.writeValueAsString(smsRequest);
            StringEntity params = new StringEntity(jsonObject);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", getAuthorization());
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            return response.getStatusLine().toString();
        } catch (Exception e) {
            return "Error " + e;
        }
    }
}