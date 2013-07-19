package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NuGetFeedDocumentTest {
    @Test
    public void shouldRejectFeedsWithMultipleEntries() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("test\\nuget-multiple-entries.xml"));
        try {
            new NuGetPackage("pkgName", "1.0").getPackageRevision(new NuGetFeedDocument(doc));
            fail("Should have thrown excption for multiple entries");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Multiple entries in feed for pkgName 1.0"));
        }
    }

    @Test
    public void shouldCreatePackageRevision() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("test\\nuget-good-feed.xml"));
        PackageRevision result = new NuGetPackage("7-Zip.CommandLine","9.20.0").getPackageRevision(new NuGetFeedDocument(doc));
        assertThat(result.getUser(), is("Igor Pavlov"));
        assertThat(result.getRevision(), is("7-Zip.CommandLine-9.20.0"));
        assertThat(result.getTimestamp(), is(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse("2013-06-09T15:36:30.807")));
        assertThat(result.getDataFor(NuGetPackage.PACKAGE_LOCATION), is("https://nuget.org/api/v2/package/7-Zip.CommandLine/9.20.0"));
        assertThat(result.getDataFor(NuGetPackage.PACKAGE_VERSIONONLY), is("9.20.0"));
    }
}
