package com.tw.go.plugin.nuget;

public class NuGetFactory {
    public NuGetCmdOutput createNuGetCmdOutputInstance(NuGetCmdParams params, ProcessOutput processOutput) {
        return new NuGetCmdOutput(params, processOutput);
    }
}
