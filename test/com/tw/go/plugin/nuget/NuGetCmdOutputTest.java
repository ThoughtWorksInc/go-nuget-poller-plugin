package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NuGetCmdOutputTest {
    private final String GET_CMD = "GET http://nuget.org/api/v2/Search()?$filter=IsLatestVersion&$orderby=Id&$skip=0&$top=30&searchTerm='7-Zip.CommandLine'&targetFramework=''&includePrerelease=false";
    private final String GET_CMD2 = "GET http://nuget.org/api/v2/Search()?$filter=IsLatestVersion&$orderby=Id&$skip=30&$top=30&searchTerm='7-Zip.CommandLine'&targetFramework=''&includePrerelease=false";
    private final String ZIP_PKG = "7-Zip.CommandLine";
    private final String VERSION = "  9.20.0";
    private final String DESCRIPTION = "  7-Zip is a file archiver with a high compression ratio.";

    @Test
    public void shouldFailIfMultiplePackagesInOutput() {
        String repoid = "repoid";
        String repourl = "http://localhost:4567/nuget/default";
        String packageId = "7-Zip.CommandLine";
        NuGetCmdParams params = new NuGetCmdParams(RepoUrl.create(repourl, null, null), packageId);
        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(GET_CMD);
        stdOut.add(ZIP_PKG);
        stdOut.add(VERSION);
        stdOut.add(DESCRIPTION);
        stdOut.add("");
        //GET Only repeats after first page
        stdOut.add("2ndpkg");
        stdOut.add("1.2");
        stdOut.add("desc");
        stdOut.add("");
        NuGetCmdOutput nuGetCmdOutput = new NuGetCmdOutput(0, stdOut, new ArrayList<String>());
        try {
            nuGetCmdOutput.validateAndParse(params);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Given PACKAGE_ID (7-Zip.CommandLine) resolves to more than one package on the repository: 7-Zip.CommandLine, 2ndpkg"));
        }

    }
    @Test

    public void shouldFailIfMultipleGETsInOutput() {
        String repourl = "http://localhost:4567/nuget/default";
        String packageId = "7-Zip.CommandLine";
        NuGetCmdParams params = new NuGetCmdParams(RepoUrl.create(repourl, null, null), packageId);
        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(GET_CMD);
        stdOut.add(ZIP_PKG);
        stdOut.add(VERSION);
        stdOut.add(DESCRIPTION);
        stdOut.add("");
        stdOut.add(GET_CMD2);
        stdOut.add("2ndpkg");
        stdOut.add("1.2");
        stdOut.add("desc");
        stdOut.add("");
        NuGetCmdOutput nuGetCmdOutput = new NuGetCmdOutput(0, stdOut, new ArrayList<String>());
        try {
            nuGetCmdOutput.validateAndParse(params);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Found more than one line starting with GET\nline0:\nGET http://nuget.org/api/v2/Search()?$filter=IsLatestVersion&$orderby=Id&$skip=0&$top=30&searchTerm='7-Zip.CommandLine'&targetFramework=''&includePrerelease=false\nline5:\nGET http://nuget.org/api/v2/Search()?$filter=IsLatestVersion&$orderby=Id&$skip=30&$top=30&searchTerm='7-Zip.CommandLine'&targetFramework=''&includePrerelease=false"));
        }

    }

    @Test
    public void shouldFailIfMultiplePackagesInOutput_NonHttp() {
        String repoid = "repoid";
        String repourl = "\\\\host\\shared-folder";
        String packageId = "7-Zip.CommandLine";
        NuGetCmdParams params = new NuGetCmdParams(RepoUrl.create(repourl, null, null), packageId);
        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(ZIP_PKG);
        stdOut.add(VERSION);
        stdOut.add(DESCRIPTION);
        stdOut.add("");
        stdOut.add("2ndpkg");
        stdOut.add("1.2");
        stdOut.add("desc");
        stdOut.add("");
        NuGetCmdOutput nuGetCmdOutput = new NuGetCmdOutputNonHttp(0, stdOut, new ArrayList<String>());
        try {
            nuGetCmdOutput.validateAndParse(params);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Given PACKAGE_ID (7-Zip.CommandLine) resolves to more than one package on the repository: 7-Zip.CommandLine, 2ndpkg"));
        }

    }

    @Test
    public void shouldFailIfNoPackageFound() {
        String repoid = "repoid";
        String repourl = "http://localhost:4567/nuget/default";
        String packageId = "7-Zip.CommandLine";
        NuGetCmdParams params = new NuGetCmdParams(RepoUrl.create(repourl, null, null), packageId);
        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(GET_CMD);
        stdOut.add("No packages found.");
        NuGetCmdOutput nuGetCmdOutput = new NuGetCmdOutput(0, stdOut, new ArrayList<String>());
        try {
            nuGetCmdOutput.validateAndParse(params);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("No package with id 7-Zip.CommandLine found in source http://localhost:4567/nuget/default"));
        }
    }

    @Test
    public void shouldFailIfNoPackageFound_NonHttp() {
        String repoid = "repoid";
        String repourl = "\\\\host\\nuget-repo";
        String packageId = "7-Zip.CommandLine";
        NuGetCmdParams params = new NuGetCmdParams(RepoUrl.create(repourl, null, null), packageId);
        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add("No packages found.");
        NuGetCmdOutput nuGetCmdOutput = new NuGetCmdOutputNonHttp(0, stdOut, new ArrayList<String>());
        try {
            nuGetCmdOutput.validateAndParse(params);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("No package with id 7-Zip.CommandLine found in source "+repourl));
        }
    }
    @Test
    public void shouldFailIfOutputDoesNotBeginWithGet() {
        String repoid = "repoid";
        String repourl = "http://localhost:4567/nuget/default";
        String packageId = "7-Zip.CommandLine";
        NuGetCmdParams params = new NuGetCmdParams(RepoUrl.create(repourl, null, null), packageId);
        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(ZIP_PKG);
        stdOut.add(VERSION);
        stdOut.add(DESCRIPTION);
        stdOut.add("");
        NuGetCmdOutput nuGetCmdOutput = new NuGetCmdOutput(0, stdOut, new ArrayList<String>());
        try {
            nuGetCmdOutput.validateAndParse(params);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Unrecognized output format. Expected GET <search-url> but was 7-Zip.CommandLine"));
        }
    }

    @Test
    public void shouldReturnCurtailedPackageRevisionForNonHttp() {
        String repoid = "repoid";
        String repourlStr = "\\\\host\\nuget-repo";
        String packageId = "7-Zip.CommandLine";
        RepoUrl repoUrl = RepoUrl.create(repourlStr, null, null);
        NuGetCmdParams params = new NuGetCmdParams(repoUrl, packageId);
        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(ZIP_PKG);
        stdOut.add(VERSION);
        stdOut.add(DESCRIPTION);
        NuGetCmdOutput nuGetCmdOutput = new NuGetCmdOutputNonHttp(0, stdOut, new ArrayList<String>());
        nuGetCmdOutput.validateAndParse(params);
        PackageRevision result = nuGetCmdOutput.getPackageRevision(repoUrl);
        assertThat(result.getUser(), is("unknown"));
        assertThat(result.getRevision(), is(ZIP_PKG+"-"+VERSION.trim()));
        assertThat(result.getTimestamp(), is(NuGetCmdOutput.MIN_DATE));
        assertThat(result.getDataFor(NuGetPackage.PACKAGE_LOCATION), is(repourlStr+"\\"+ZIP_PKG+"."+VERSION.trim()+".nupkg"));
        assertThat(result.getDataFor(NuGetPackage.PACKAGE_DESCRIPTION), is(DESCRIPTION.trim()));
    }
}
