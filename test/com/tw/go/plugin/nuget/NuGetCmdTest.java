package com.tw.go.plugin.nuget;

import com.tw.go.plugin.nuget.config.RepoUrl;
import com.tw.go.plugin.util.StringUtil;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class NuGetCmdTest {
    private final String GET_CMD = "GET http://nuget.org/api/v2/Search()?$filter=IsLatestVersion&$orderby=Id&$skip=0&$top=30&searchTerm='7-Zip.CommandLine'&targetFramework=''&includePrerelease=false";
    private final String ZIP_PKG = "7-Zip.CommandLine";
    private final String VERSION = "  9.20.0";
    private final String DESCRIPTION = "  7-Zip is a file archiver with a high compression ratio.";

    @Test
    public void executeShouldGetPackageRevisionIfExecSucceeds() {
        ProcessRunner processRunner = mock(ProcessRunner.class);
        String repoid = "repoid";
        String repourl = "http://localhost:4567/nuget/default";
        String spec = "7-Zip.CommandLine";
        String[] expectedCommand = {"nuget", "list", "Id:" + spec, "-Verbosity", "detailed", "-Source", repourl};

        ArrayList<String> stdOut = new ArrayList<String>();
        stdOut.add(GET_CMD);
        stdOut.add(ZIP_PKG);
        stdOut.add(VERSION);
        stdOut.add(DESCRIPTION);
        stdOut.add("");

        NuGetCmdOutput nuGetCmdOutput = mock(NuGetCmdOutput.class);
        when(processRunner.execute(expectedCommand, true)).thenReturn(nuGetCmdOutput);
        NuGetCmdParams params = new NuGetCmdParams(repoid, RepoUrl.create(repourl, null, null), spec);
        when(nuGetCmdOutput.isSuccess()).thenReturn(true);
        new NuGetCmd(processRunner, params).execute();

        verify(processRunner).execute(expectedCommand, true);
        verify(nuGetCmdOutput).getPackageRevision(repourl);
    }

    @Test
    public void shouldThrowExceptionIfCommandFails() {
        ProcessRunner processRunner = mock(ProcessRunner.class);
        ArrayList<String> stdErr = new ArrayList<String>();
        stdErr.add("err msg");
        when(processRunner.execute(Matchers.<String[]>any(), eq(true))).thenReturn(new NuGetCmdOutput(1, null, stdErr));
        try {
            new NuGetCmd(processRunner, new NuGetCmdParams("repoid", RepoUrl.create("http://url", null, null), "spec")).execute();
            fail("expected exception");
        } catch (Exception success) {
            assertThat(success.getMessage(), is("Error while querying repository with path 'http://url' and package spec 'spec'. Error Message: err msg"));
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
        String repoId = UUID.randomUUID().toString();
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 1; i++) {
            Thread thread = new Thread(new CommandThread(repoId));
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
        private String repoId;
        private String repoUrl;

        CommandThread(String repoId) {
            this.repoId = repoId;
            repoUrl = "file://d:/tmp/nuget-local-repo";//"http://localhost:4567/nuget/default";
        }

        public void run() {
            new NuGetCmd(new NuGetCmdParams(repoId, RepoUrl.create(repoUrl, null, null), "7-Zip.CommandLine")).execute();
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}
