package com.tw.go.plugin.nuget;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class HttpConnectionChecker implements ConnectionChecker {

    public void checkConnection(String url, Credentials credentials) {
        DefaultHttpClient client = new DefaultHttpClient();
        if (credentials.provided()) {
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(credentials.getUser(), credentials.getPassword());
            //setAuthenticationPreemptive
            client.getCredentialsProvider().setCredentials(AuthScope.ANY, usernamePasswordCredentials);
        }
        HttpGet method = new HttpGet(url);
        try {
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(response.getStatusLine().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            method.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

}
