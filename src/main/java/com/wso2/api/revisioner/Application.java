package com.wso2.api.revisioner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

import com.wso2.api.revisioner.utils.FileUtils;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.json.JSONObject;
import org.springframework.context.ConfigurableApplicationContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SpringBootApplication
public class Application {

    private static String HOST = "";
    private static String TRANSPORT_PORT = "";
    private static String CLIENT_KEY = "";
    private static String CLIENT_SECRET = "";
    private static String SANDBOX_ENDPOINT = "";
    private static String PRODUCTION_ENDPOINT = "";
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static String API_LIMIT = "";

    private static String APPLICATION_HOST = "application.host";
    private static String APPLICATION_TRANSPORT_PORT = "application.transport.port";
    private static String APPLICATION_CLIENT_KEY = "application.client.key";
    private static String APPLICATION_CLIENT_SECRET = "application.client.secret";
    private static String APPLICATION_SANDBOX_ENDPOINT = "application.sandbox.endpoint";
    private static String APPLICATION_PRODUCTION_ENDPOINT = "application.production.endpoint";
    private static String APPLICATION_USERNAME = "application.username";
    private static String APPLICATION_PASSWORD = "application.password";
    private static String APPLICATION_API_LIMIT = "application.api.limit";

    private static String GATEWAY_NAME = "";
    private static String VHOST = "";
    private static boolean DISPLAY_ON_DEV_PORTAL = true;

    static {
        Properties properties = FileUtils.readConfiguration();
        HOST = properties.getProperty(APPLICATION_HOST, "");
        TRANSPORT_PORT = properties.getProperty(APPLICATION_TRANSPORT_PORT, "");
        CLIENT_KEY = properties.getProperty(APPLICATION_CLIENT_KEY, "");
        CLIENT_SECRET = properties.getProperty(APPLICATION_CLIENT_SECRET, "");
        SANDBOX_ENDPOINT = properties.getProperty(APPLICATION_SANDBOX_ENDPOINT, "");
        PRODUCTION_ENDPOINT = properties.getProperty(APPLICATION_PRODUCTION_ENDPOINT, "");
        USERNAME = properties.getProperty(APPLICATION_USERNAME, "");
        PASSWORD = properties.getProperty(APPLICATION_PASSWORD, "");
        API_LIMIT = properties.getProperty(APPLICATION_API_LIMIT, "");
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            fw = FileUtils.getNewFileWriter();
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
            System.out.println("HOST : " + HOST);
            System.out.println("TRANSPORT_PORT : " + TRANSPORT_PORT);
            System.out.println("CLIENT_KEY : " + CLIENT_KEY);
            System.out.println("CLIENT_SECRET : " + CLIENT_SECRET);
            System.out.println("SANDBOX_ENDPOINT : " + SANDBOX_ENDPOINT);
            System.out.println("PRODUCTION_ENDPOINT : " + PRODUCTION_ENDPOINT);
            System.out.println("USERNAME : " + USERNAME);
            System.out.println("PASSWORD : " + PASSWORD);
            System.out.println("API_LIMIT : " + API_LIMIT);

            pw.println("HOST : " + HOST);
            pw.println("TRANSPORT_PORT : " + TRANSPORT_PORT);
            pw.println("CLIENT_KEY : " + CLIENT_KEY);
            pw.println("CLIENT_SECRET : " + CLIENT_SECRET);
            pw.println("SANDBOX_ENDPOINT : " + SANDBOX_ENDPOINT);
            pw.println("PRODUCTION_ENDPOINT : " + PRODUCTION_ENDPOINT);
            pw.println("USERNAME : " + USERNAME);
            pw.println("PASSWORD : " + PASSWORD);
            pw.println("API_LIMIT : " + API_LIMIT);

            if (!HOST.equals("") && !TRANSPORT_PORT.equals("") && !CLIENT_KEY.equals("") && !CLIENT_SECRET.equals("")
                    && !SANDBOX_ENDPOINT.equals("") && !PRODUCTION_ENDPOINT.equals("") && !USERNAME.equals("")
                    && !PASSWORD.equals("") && !API_LIMIT.equals("")) {
                String accessToken = generateAccessToken(pw);
                System.out.println("*************************************************************");
                pw.println("*************************************************************");
                if (accessToken != null) {
                    List<String> apiIds = retrieveAllAPIIds(accessToken, pw);
                    if (apiIds.size() != 0) {
                        System.out.println("*************************************************************");
                        pw.println("*************************************************************");
                        System.out.println("Total Number of APIs to be changed : " + apiIds.size());
                        pw.println("Total Number of APIs to be changed : " + apiIds.size());
                        System.out.println("*************************************************************");
                        pw.println("*************************************************************");
                        for (int i = 0; i < apiIds.size(); i++) {
                            JSONObject apiDefinition = getAPIById(accessToken, apiIds.get(i), pw);
//
                            apiDefinition = changeEPParameter(apiIds.get(i), apiDefinition, pw);

                            if (apiDefinition == null) {
                                System.out.println("Skipping updating the endpoints of the API : " + apiIds.get(i));
                                pw.println("Skipping updating the endpoints of the API : " + apiIds.get(i));
                                System.out.println("*************************************************************");
                                pw.println("*************************************************************");
                                continue;
                            }
//
                            if (updateApi(accessToken, apiIds.get(i), apiDefinition, pw)) {
                                System.out.println("Update API Operation Status : " + true);
                                pw.println("Update API Operation Status : " + true);
                                String inactiveRevision = getInactiveRevision(accessToken, apiIds.get(i), pw);
//
                                if (inactiveRevision != null) {
                                    if (deleteRevision(accessToken, apiIds.get(i), inactiveRevision, pw)) {
                                        System.out.println("Delete Revision Operation Status : " + true);
                                        pw.println("Delete Revision Operation Status : " + true);
                                        String revisionId = createRevision(accessToken, apiIds.get(i), pw);
                                        if (deployRevision(accessToken, apiIds.get(i), revisionId, pw)) {
                                            System.out.println("Deploying the Revision : " + revisionId + " is success.");
                                            pw.println("Deploying the Revision : " + revisionId + " is success.");
                                        } else {
                                            System.out.println("Deploying the Revision : " + revisionId + " was not success.");
                                            pw.println("Deploying the Revision : " + revisionId + " was not success.");
                                        }
                                    } else {
                                        System.out.println("Deleting the Revision : " + inactiveRevision + " was not success.");
                                        pw.println("Deleting the Revision : " + inactiveRevision + " was not success.");
                                    }
                                } else {
                                    String revisionId = createRevision(accessToken, apiIds.get(i), pw);
                                    if (deployRevision(accessToken, apiIds.get(i), revisionId, pw)) {
                                        System.out.println("Deploying the Revision : " + revisionId + " is success.");
                                        pw.println("Deploying the Revision : " + revisionId + " is success.");

                                    } else {
                                        System.out.println("Deploying the Revision : " + revisionId + " was not success.");
                                        pw.println("Deploying the Revision : " + revisionId + " was not success.");
                                    }
                                }
                            } else {
                                System.out.println("Updating the API : " + apiIds.get(i) + " was not success.");
                                pw.println("Updating the API : " + apiIds.get(i) + " was not success.");
                            }
                            System.out.println("*************************************************************");
                            pw.println("*************************************************************");
                        }
                    }
                }
            } else {
                System.out.println("Parameters were not loaded correctly. Please check the integration.properties file.");
                pw.println("Parameters were not loaded correctly. Please check the integration.properties file.");
            }
            pw.flush();
        } finally {
            try {
                pw.close();
                bw.close();
                fw.close();
                ctx.close();
            } catch (IOException io) {
                // can't do anything
                ctx.close();
            } catch (Exception e) {
                ctx.close();
            }
        }
    }

    private static String generateAccessToken(PrintWriter pw) {
        final String REQUEST_BODY = "{\"grant_type\": \"password\",\n" +
                "\"username\":\"" + USERNAME + "\",\n" +
                "\"password\":\"" + PASSWORD + "\",\n" +
                "\"scope\":\"apim:api_view apim:api_create apim:api_manage apim:api_delete apim:api_publish apim:subscription_view apim:subscription_block apim:subscription_manage apim:external_services_discover apim:threat_protection_policy_create apim:threat_protection_policy_manage apim:document_create apim:document_manage apim:mediation_policy_view apim:mediation_policy_create apim:mediation_policy_manage apim:client_certificates_view apim:client_certificates_add apim:client_certificates_update apim:ep_certificates_view apim:ep_certificates_add apim:ep_certificates_update apim:publisher_settings apim:pub_alert_manage apim:shared_scope_manage apim:app_import_export apim:api_import_export apim:api_product_import_export apim:api_generate_key apim:common_operation_policy_view apim:common_operation_policy_manage apim:comment_write apim:comment_view apim:admin\"\n" +
                "}";

        String appCredentials = CLIENT_KEY + ":" + CLIENT_SECRET;
        String encodedString = Base64.getEncoder().encodeToString(appCredentials.getBytes());

        HttpPost httpPost = new HttpPost("https://" + HOST + ":" + TRANSPORT_PORT + "/oauth2/token");
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(REQUEST_BODY);
            httpPost.setEntity(entity);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedString);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    //System.out.println("FULL TOKEN RESPONSE : " + sb.toString());
                    JSONObject object = new JSONObject(sb.toString());
                    String accessToken = (String) object.get("access_token");
                    System.out.println("ACCESS TOKEN : " + accessToken);
                    pw.println("ACCESS TOKEN : " + accessToken);
                    return accessToken;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the token service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the token service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
            } else {
                System.out.println("Generate Token Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Generate Token Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> retrieveAllAPIIds(String accessToken, PrintWriter pw) {
        HttpGet httpGet = new HttpGet("https://" + HOST + ":" + TRANSPORT_PORT + "/api/am/publisher/v3/apis?limit=" + API_LIMIT);
        try {
            httpGet.setHeader("Accept", "application/json");

            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Response : " + sb.toString());
                    JSONObject object = new JSONObject(sb.toString());
                    List<String> apiObjArr = new ArrayList<>();
                    JSONArray jsonArray = object.getJSONArray("list");
//                    System.out.println("List of All APIs : " + jsonArray);
                    if (jsonArray != null) {
                        //Iterating JSON array
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //Adding each element of JSON array into ArrayList
                            JSONObject tempObj = (JSONObject) jsonArray.get(i);
                            String lifeCycleStatus = tempObj.getString("lifeCycleStatus");
                            String type = tempObj.getString("type");
                            if (!lifeCycleStatus.equals("DEPRECATED") && !lifeCycleStatus.equals("RETIRED")) {
                                if (!type.equals("WS")) {
                                    apiObjArr.add((String) tempObj.get("id"));
                                } else {
                                    System.out.println("WebSocket API is identified : " + tempObj.getString("id") + ". Hence, skipping updating the endpoints of the API");
                                    pw.println("WebSocket API is identified : " + tempObj.getString("id") + ". Hence, skipping updating the endpoints of the API");
                                }
                            } else {
                                System.out.println("Identified the API : " + tempObj.getString("id") + " is " + lifeCycleStatus);
                                pw.println("Identified the API : " + tempObj.getString("id") + " is " + lifeCycleStatus);
                            }
                        }
                    }
//                    System.out.println("All API Ids : "+apiObjArr.toString());
                    return apiObjArr;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher get all api service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher get all api service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
            } else {
                System.out.println("Retrieve All APIs Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Retrieve All APIs Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject getAPIById(String accessToken, String apiId, PrintWriter pw) {
        HttpGet httpGet = new HttpGet("https://" + HOST + ":" + TRANSPORT_PORT + "/api/am/publisher/v3/apis/" + apiId);
        try {
            httpGet.setHeader("Accept", "application/json");

            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full API Response : " + sb.toString());
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    return jsonObject;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher get api service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher get api service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
            } else {
                System.out.println("Get API by ID Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Get API by ID Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject changeEPParameter(String apiId, JSONObject apiDefinition, PrintWriter pw) {
        try {
            System.out.println("Sandbox and production endpoints are being changed for the API : " + apiId);
            pw.println("Sandbox and production endpoints are being changed for the API : " + apiId);

            String regex = "((http|https):\\/\\/(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)*[a-zA-Z0-9-]+(:[0-9]{1,5})*)";
            if (!apiDefinition.get("endpointConfig").equals(null)) {
                JSONObject endpointConfig = (JSONObject) apiDefinition.get("endpointConfig");
                if (endpointConfig.has("sandbox_endpoints")) {
                    JSONObject sandboxEP = (JSONObject) endpointConfig.get("sandbox_endpoints");
                    String sandboxUrl = sandboxEP.getString("url");
                    String[] splitSandboxUrl = sandboxUrl.split(regex);
                    if (splitSandboxUrl.length > 0) {
                        sandboxEP.put("url", SANDBOX_ENDPOINT + splitSandboxUrl[1]);
                    } else {
                        sandboxEP.put("url", SANDBOX_ENDPOINT);
                    }
                } else {
                    System.out.println("Sandbox endpoint is null for the API : " + apiId);
                    pw.println("Sandbox endpoint is null for the API : " + apiId);
                }

                if (endpointConfig.has("production_endpoints")) {
                    JSONObject prodEP = (JSONObject) endpointConfig.get("production_endpoints");
                    String prodUrl = prodEP.getString("url");
                    String[] splitProdUrl = prodUrl.split(regex);
                    if (splitProdUrl.length > 0) {
                        prodEP.put("url", PRODUCTION_ENDPOINT + splitProdUrl[1]);
                    } else {
                        prodEP.put("url", PRODUCTION_ENDPOINT);
                    }
                } else {
                    System.out.println("Production endpoint is null for the API : " + apiId);
                    pw.println("Production endpoint is null for the API : " + apiId);
                }
            } else {
                System.out.println("Both Production and Sandbox endpoints are null for the API : " + apiId);
                pw.println("Both Production and Sandbox endpoints are null for the API : " + apiId);
                return null;
            }

        } catch (JSONException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
        return apiDefinition;
    }

    private static boolean updateApi(String accessToken, String apiId, JSONObject apiDefinition, PrintWriter pw) {


        HttpPut httpPut = new HttpPut("https://" + HOST + ":" + TRANSPORT_PORT + "/api/am/publisher/v3/apis/" + apiId);
        try {
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(apiDefinition.toString());
            httpPut.setEntity(entity);

            httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPut);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Updated API Response : " + sb.toString());
                    return true;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher update api service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher update api service : " + e);
                    return false;
                } catch (Exception e) {
                    return false;
                }
            } else {
                System.out.println("Update API Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Update API Returned Status Code : " + response.getStatusLine().getStatusCode());
                return false;
            }

        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (KeyManagementException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static String createRevision(String accessToken, String apiId, PrintWriter pw) {

        HttpPost httpPost = new HttpPost("https://" + HOST + ":" + TRANSPORT_PORT + "/api/am/publisher/v3/apis/" + apiId + "/revisions");
        final String REQUEST_BODY = "{\n" +
                "\"description\": \"Sandbox and Production Endpoints have been changed in the API : " + apiId + "\"\n" +
                "}";
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(REQUEST_BODY);
            httpPost.setEntity(entity);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 201) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Create Revision Response : " + sb.toString());
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    System.out.println("Newly created revision Id : " + jsonObject.get("id"));
                    pw.println("Newly created revision Id : " + jsonObject.get("id"));
                    return (String) jsonObject.get("id");

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher create revision service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher create revision service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
            } else {
                System.out.println("Create Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Create Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean deployRevision(String accessToken, String apiId, String revisionId, PrintWriter pw) {
        HttpPost httpPost = new HttpPost("https://" + HOST + ":" + TRANSPORT_PORT + "/api/am/publisher/v3/apis/" + apiId + "/deploy-revision?revisionId=" + revisionId);

        System.out.println("Gateway Name : " + GATEWAY_NAME);
        System.out.println("VHOST : " + VHOST);
        System.out.println("Display on DevPortal : " + DISPLAY_ON_DEV_PORTAL);
        final String REQUEST_BODY = "[\n" +
                "    {\n" +
                "        \"name\": \"" + GATEWAY_NAME + "\",\n" +
                "        \"vhost\": \"" + VHOST + "\",\n" +
                "        \"displayOnDevportal\": " + DISPLAY_ON_DEV_PORTAL + "\n" +
                "    }\n" +
                "]";
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(REQUEST_BODY);
            httpPost.setEntity(entity);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 201) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Deploy Response : " + sb.toString());
                    JSONArray jsonArray = new JSONArray(sb.toString());
                    JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                    return true;
                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the authorization service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the authorization service : " + e);
                    return false;
                } catch (JSONException e) {
                    return false;
                } catch (Exception e) {
                    return false;
                }
            } else {
                System.out.println("Deploy Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Deploy Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return false;
            }

        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (KeyManagementException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getInactiveRevision(String accessToken, String apiId, PrintWriter pw) {
        HttpGet httpGet = new HttpGet("https://" + HOST + ":" + TRANSPORT_PORT + "/api/am/publisher/v3/apis/" + apiId + "/revisions");
        try {
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Revision Response : " + sb.toString());
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    int numberOfRevisions = (int) jsonObject.get("count");
                    if (numberOfRevisions > 4) {
                        JSONArray jsonArray = (JSONArray) jsonObject.get("list");
                        TreeMap<Long, String> dateRevisionMap = new TreeMap<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                            JSONArray deploymentInfoArr = (JSONArray) jsonObject1.get("deploymentInfo");
                            if (deploymentInfoArr.length() < 1) {
                                dateRevisionMap.put((Long) jsonObject1.get("createdTime"), jsonObject1.getString("id"));
                            } else {
                                //Need to be careful if multiple deployment information are there!
                                JSONObject jsonObject2 = (JSONObject) deploymentInfoArr.get(0);
                                GATEWAY_NAME = jsonObject2.getString("name");
                                VHOST = jsonObject2.getString("vhost");
                                DISPLAY_ON_DEV_PORTAL = jsonObject2.getBoolean("displayOnDevportal");
                            }
                        }
                        System.out.println("Revision selected to delete : " + dateRevisionMap.firstEntry().getValue());
                        pw.println("Revision selected to delete : " + dateRevisionMap.firstEntry().getValue());
                        return dateRevisionMap.firstEntry().getValue();
                    } else {
                        JSONArray jsonArray = (JSONArray) jsonObject.get("list");
                        TreeMap<Long, String> dateRevisionMap = new TreeMap<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                            JSONArray deploymentInfoArr = (JSONArray) jsonObject1.get("deploymentInfo");
                            if (deploymentInfoArr.length() > 0) {
                                //Need to be careful if multiple deployment information are there!
                                JSONObject jsonObject2 = (JSONObject) deploymentInfoArr.get(0);
                                GATEWAY_NAME = jsonObject2.getString("name");
                                VHOST = jsonObject2.getString("vhost");
                                DISPLAY_ON_DEV_PORTAL = jsonObject2.getBoolean("displayOnDevportal");
                            }
                        }
                    }
                    return null;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher get revision service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher get revision service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                }
            } else {
                System.out.println("Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Get Inactive Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean deleteRevision(String accessToken, String apiId, String revisionId, PrintWriter pw) {
        HttpDelete httpDelete = new HttpDelete("https://" + HOST + ":" + TRANSPORT_PORT + "/api/am/publisher/v3/apis/" + apiId + "/revisions/" + revisionId);

        try {
            httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpDelete);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Revision Delete Response : " + sb.toString());
                    return true;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher revision delete service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher revision delete service : " + e);
                    return false;
                } catch (Exception e) {
                    return false;
                }
            } else {
                System.out.println("Delete Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Delete Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return false;
            }

        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (KeyManagementException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
