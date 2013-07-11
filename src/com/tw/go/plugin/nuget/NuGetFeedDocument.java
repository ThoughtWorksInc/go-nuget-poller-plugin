package com.tw.go.plugin.nuget;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Date;

public class NuGetFeedDocument {
    public static final String SCHEMA_ADO_DATASERVICES = "http://schemas.microsoft.com/ado/2007/08/dataservices";
    public static final String SCHEMA_ADO_DATASERVICES_METADATA = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
    private final Document xmlFeed;

    public NuGetFeedDocument(Document xmlDocument) {
        this.xmlFeed = xmlDocument;
    }

    String getPackageLocation() {
        return firstOf(xmlFeed.getElementsByTagName("content")).getAttributes().getNamedItem("src").getTextContent();
    }

    String getAuthor() {
        return xmlFeed.getElementsByTagName("name").item(0).getTextContent();
    }

    Date getPublishedDate(NodeList properties) {
        return javax.xml.bind.DatatypeConverter.parseDateTime(getProperty(properties, "Published")).getTime();
    }

    String getProperty(NodeList properties, String name) {
        return firstOf(properties).getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES, name).item(0).getTextContent();
    }

    NodeList getProperties() {
        return xmlFeed.getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES_METADATA, "properties");
    }

    String getEntryTitle(NodeList entries) {
        NodeList titles = firstOf(entries).getElementsByTagName("title");
        return titles.item(0).getTextContent();
    }

    NodeList getEntries() {
        return xmlFeed.getElementsByTagName("entry");
    }

    private Element firstOf(NodeList nodeList) {
        return ((Element) nodeList.item(0));
    }
}
