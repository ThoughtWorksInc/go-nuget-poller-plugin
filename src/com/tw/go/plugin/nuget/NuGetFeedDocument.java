package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
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

    Date getPublishedDate() {
        return javax.xml.bind.DatatypeConverter.parseDateTime(getProperty(getProperties(), "Published")).getTime();
    }

    private String getProperty(NodeList properties, String name) {
        return firstOf(properties).getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES, name).item(0).getTextContent();
    }

    private NodeList getProperties() {
        return xmlFeed.getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES_METADATA, "properties");
    }

    String getEntryTitle() {
        NodeList titles = firstOf(getEntries()).getElementsByTagName("title");
        return titles.item(0).getTextContent();
    }

    NodeList getEntries() {
        return xmlFeed.getElementsByTagName("entry");
    }

    private Element firstOf(NodeList nodeList) {
        return ((Element) nodeList.item(0));
    }

    public String getPackageVersion() {
        return getProperty(getProperties(), "Version");
    }

    public PackageRevision getPackageRevision(boolean lastVersionKnown) {
        if (getEntries().getLength() == 0){
            if(lastVersionKnown) return null;
            else throw new NuGetException("No such package found");
        }
        if (getEntries().getLength() > 1)
            throw new NuGetException(String.format("Multiple entries in feed for %s %s", getEntryTitle(), getPackageVersion()));
        PackageRevision result = new PackageRevision(getPackageLabel(), getPublishedDate(), getAuthor());
        result.addData(NuGetPackage.PACKAGE_LOCATION, getPackageLocation());
        result.addData(NuGetPackage.PACKAGE_DESCRIPTION, getDescriptionSummary());
        result.addData(NuGetPackage.PACKAGE_VERSION, getPackageVersion());
        return result;
    }

    private String getDescriptionSummary() {
        String summary = getEntrySummary();
        if(summary == null || summary.trim().isEmpty()) return getFirstLineOfDescription();
        return summary;
    }

    private String getFirstLineOfDescription() {
        String description = getProperty(getProperties(), "Description");
        return description.split("\n")[0];
    }

    private String getEntrySummary() {
        NodeList summary = firstOf(getEntries()).getElementsByTagName("summary");
        return summary.item(0).getTextContent();
    }

    private String getPackageLabel() {
        return getEntryTitle() + "-" + getPackageVersion();
    }
}
