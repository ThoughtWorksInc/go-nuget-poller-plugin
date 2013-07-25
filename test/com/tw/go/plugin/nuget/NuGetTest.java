package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;
import com.tw.go.plugin.nuget.exe.NuGetCmdOutput;
import com.tw.go.plugin.nuget.exe.ProcessRunner;
import com.tw.go.plugin.util.StringUtil;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_LOCATION;
import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_VERSIONONLY;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class NuGetTest {
    private final String GET_CMD = "GET http://nuget.org/api/v2/Search()?$filter=IsLatestVersion&$orderby=Id&$skip=0&$top=30&searchTerm='7-Zip.CommandLine'&targetFramework=''&includePrerelease=false";
    private final String ZIP_PKG = "7-Zip.CommandLine";
    private final String VERSION = "  9.20.0";
    private final String DESCRIPTION = "  7-Zip is a file archiver with a high compression ratio.";

    @Test
    public void executeShouldGetPackageRevisionIfExecSucceeds() {
        ProcessRunner processRunner = mock(ProcessRunner.class);
        String repourlStr = "http://localhost:4567/nuget/default";
        String packageId = "7-Zip.CommandLine";
        String[] expectedCommand = {"nuget", "list", "Id:" + packageId, "-Verbosity", "detailed", "-Source", repourlStr};

        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(GET_CMD);
        stdOut.add(ZIP_PKG);
        stdOut.add(VERSION);
        stdOut.add(DESCRIPTION);
        stdOut.add("");

        NuGetCmdOutput nuGetCmdOutput = mock(NuGetCmdOutput.class);
        when(processRunner.execute(expectedCommand, true)).thenReturn(nuGetCmdOutput);
        RepoUrl repoUrl = RepoUrl.create(repourlStr, null, null);
        NuGetParams params = new NuGetParams(repoUrl, packageId, null, null, null, true);
        when(nuGetCmdOutput.isSuccess()).thenReturn(true);
        new NuGet(processRunner, params).poll();

        verify(processRunner).execute(expectedCommand, true);
        verify(nuGetCmdOutput).getPackageRevision(repoUrl);
    }

    @Test
    public void shouldThrowExceptionIfCommandFails() {
        ProcessRunner processRunner = mock(ProcessRunner.class);
        ArrayList<String> stdErr = new ArrayList<String>();
        stdErr.add("err msg");
        when(processRunner.execute(Matchers.<String[]>any(), eq(true))).thenReturn(new NuGetCmdOutput(1, null, stdErr));
        try {
            new NuGet(processRunner, new NuGetParams(RepoUrl.create("http://url", null, null), "wix", null, null, null, true)).poll();
            fail("expected exception");
        } catch (Exception success) {
            assertThat(success.getMessage(),
                    is("Falling back to nuget.exe after API error: RuntimeException: UnknownHostException (url) while getting package feed for : http://url/GetUpdates()?packageIds='wix'&versions='0.0.1'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1 \nnuget.exe: Error while querying repository with path 'http://url' and packageId 'wix'. Error Message: err msg"));
        }
        verify(processRunner).execute(Matchers.<String[]>any(), eq(true));
    }

    @Test
    @Ignore
    public void shouldHandleMultipleThreads() throws InterruptedException {
        final StringBuilder errors = new StringBuilder();
        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                errors.append(t.getName()).append(" : ").append(e.getMessage());
            }
        };
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new CommandThread());
            thread.setUncaughtExceptionHandler(handler);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        if (!StringUtil.isBlank(errors.toString())) {
            fail(errors.toString());
        }
    }

    class CommandThread implements Runnable {
        private String repoUrl;

        CommandThread() {
            //            repoUrl = "https://nuget.org/api/v2";
            repoUrl = "file://d:/tmp/nuget-local-repo";
        }

        public void run() {
            PackageRevision result = new NuGet(new NuGetParams(RepoUrl.create(repoUrl, null, null), "RouteMagic", null, null, null, true)).poll();
            System.out.println(result.getRevision());
            System.out.println(result.getDataFor(PACKAGE_LOCATION));
            System.out.println(result.getDataFor(NuGetPackage.PACKAGE_DESCRIPTION));
        }
    }

    @Test
    public void shouldReportLocationCorrectly(){
        PackageRevision result = new NuGet(new NuGetParams(RepoUrl.create("file://d:/tmp/nuget-local-repo", null, null), "RouteMagic", null, null, null, true)).poll();
        assertThat(result.getDataFor(PACKAGE_LOCATION), is("file://d:/tmp/nuget-local-repo/RouteMagic.1.2.nupkg"));
        result = new NuGet(new NuGetParams(RepoUrl.create("\\\\insrinaray\\nuget-local-repo", null, null), "RouteMagic", null, null, null, true)).poll();
        assertThat(result.getDataFor(PACKAGE_LOCATION), is("\\\\insrinaray\\nuget-local-repo\\RouteMagic.1.2.nupkg"));
        result = new NuGet(new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null), "RouteMagic.Mvc", null, null, null, true)).poll();
        assertThat(result.getDataFor(PACKAGE_LOCATION), is("http://www.nuget.org/api/v2/package/RouteMagic.Mvc/1.2"));
    }


    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    @Test
    public void shouldFailIfNoPackagesFound(){
        expectedEx.expect(NuGetException.class);
        expectedEx.expectMessage("No such package found");
        new NuGet(new NuGetParams(RepoUrl.create("http://nuget.org/api/v2/", null, null), "Rou", null, null, null, true)).poll();
    }

    @Test
    public void shouldGetUpdateWhenLastVersionKnown() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("1Password-1.0.9.288", new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSIONONLY, "1.0.9.288");
        PackageRevision result = new NuGet(new NuGetParams(RepoUrl.create("http://chocolatey.org/api/v2", null, null), "1Password", null, null, lastKnownVersion, true)).poll();
        assertThat(result.getDataFor(PACKAGE_VERSIONONLY), is("1.0.9.332"));
    }

    @Test
    public void shouldReturnNullIfNoNewerRevision() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("1Password-1.0.9.332", new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSIONONLY, "1.0.9.332");
        NuGetParams params = new NuGetParams(RepoUrl.create("http://chocolatey.org/api/v2", null, null), "1Password", null, null, lastKnownVersion, true);
        assertNull(new NuGet(params).poll());

    }
}
