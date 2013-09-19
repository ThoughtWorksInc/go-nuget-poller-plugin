package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
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
        Document doc = builder.parse(new File("test\\fast\\nuget-multiple-entries.xml"));
        try {
            new NuGetFeedDocument(doc).getPackageRevision(false);
            fail("Should have thrown excption for multiple entries");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Multiple entries in feed for 7-Zip.CommandLine 9.20.0"));
        }
    }

    @Test
    public void shouldCreatePackageRevision() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("test\\fast\\nuget-good-feed.xml"));
        PackageRevision result = new NuGetFeedDocument(doc).getPackageRevision(false);
        assertThat(result.getUser(), is("Igor Pavlov"));
        assertThat(result.getRevision(), is("7-Zip.CommandLine-9.20.0"));
        assertThat(result.getRevisionComment(), is("revision comment line 1"));
        assertThat(result.getTimestamp(), is(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse("2013-06-09T15:36:30.807")));
        assertThat(result.getDataFor(NuGetPackageConfig.PACKAGE_LOCATION), is("https://nuget.org/api/v2/package/7-Zip.CommandLine/9.20.0"));
        assertThat(result.getDataFor(NuGetPackageConfig.PACKAGE_VERSION), is("9.20.0"));
        assertThat(result.getDataFor(NuGetPackageConfig.PACKAGE_DESCRIPTION), is("7-Zip is a file archiver with a high compression ratio."));
    }
}
