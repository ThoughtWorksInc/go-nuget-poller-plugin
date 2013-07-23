package com.tw.go.plugin.nuget;

import com.tw.go.plugin.nuget.config.HttpRepoURL;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;

public class Feed {
    private final String url;
    //TODO:ensure not more than 3 downloads in progress - won't happen for the same repo, ensure for different

    public Feed(String url) {
        this.url = url;
    }

    public Document download() {
        DefaultHttpClient client = HttpRepoURL.getHttpClient();
        HttpGet method = new HttpGet(url);
        method.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
        try {
            HttpResponse response = client.execute(method);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            return builder.parse(response.getEntity().getContent());
        } catch (Exception ex) {
            throw new RuntimeException(String.format("%s (%s) while getting package feed for : %s ", ex.getClass().getSimpleName(),ex.getMessage(), url), ex);
        } finally {
            method.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

}


