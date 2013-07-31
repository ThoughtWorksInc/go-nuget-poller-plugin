package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreConnectionPNames;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpRepoURL extends RepoUrl {

    public HttpRepoURL(String url, String user, String password) {
        super(url, user, password);
    }

    public static DefaultHttpClient getHttpClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,5*1000);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3,false));
        return client;
    }

    public void validate(Errors errors) {
        try {
            doBasicValidations(errors);
            URL validatedUrl = new URL(this.url);
            if (!(validatedUrl.getProtocol().startsWith("http"))) {
                errors.addError(new ValidationError(REPO_URL, "Invalid URL: Only http is supported."));
            }

            if (StringUtil.isNotBlank(validatedUrl.getUserInfo())) {
                errors.addError(new ValidationError(REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
            }
            credentials.validate(errors);
        } catch (MalformedURLException e) {
            errors.addError(new ValidationError(REPO_URL, "Invalid URL : " + url));
        }
    }


    public void checkConnection() {
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
        } finally {
            method.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

    public String getUrlWithBasicAuth() {
        String localUrl = this.url;
        try {
            new URL(localUrl);
            if (credentials.provided()) {
                String[] parts = localUrl.split("//");
                if (parts.length != 2) throw new RuntimeException(String.format("Invalid uri format %s", this.url));
                localUrl = parts[0] + "//" + credentials.getUserInfo() + "@" + parts[1];
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return localUrl;
    }

}
