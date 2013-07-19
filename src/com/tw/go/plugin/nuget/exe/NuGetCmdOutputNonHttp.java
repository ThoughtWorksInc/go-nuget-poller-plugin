package com.tw.go.plugin.nuget.exe;

import java.util.List;

public class NuGetCmdOutputNonHttp extends NuGetCmdOutput {
    public NuGetCmdOutputNonHttp(int returnCode, List<String> stdOut, List<String> stdErr) {
        super(returnCode, stdOut, stdErr);
    }

    @Override
    protected String getFirstLineOfDescription() {
        return stdOut.get(2).trim();
    }

    @Override
    protected String getPackageVersion() {
        return stdOut.get(1).trim();
    }

    @Override
    protected String getPackageTitle() {
        return stdOut.get(0).trim();
    }

    @Override
    protected boolean noPackagesFound() {
        return "No packages found.".equals(stdOut.get(0).trim());
    }
}
