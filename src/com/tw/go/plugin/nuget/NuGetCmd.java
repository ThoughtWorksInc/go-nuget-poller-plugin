package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

import static java.lang.String.format;

public class NuGetCmd {
    static final String DELIMITER = "<=>";
    private final ProcessRunner processRunner;
    private static Logger LOGGER = Logger.getLoggerFor(NuGetCmd.class);
    private final NuGetCmdParams params;

    public NuGetCmd(NuGetCmdParams params) {
        this(new ProcessRunner(), params);
    }

    //for tests
    public NuGetCmd(ProcessRunner processRunner, NuGetCmdParams params) {
        this.processRunner = processRunner;
        this.params = params;
    }

    public PackageRevision execute() {
        String[] command = {"nuget", "list", params.getApplicablePackageSpec(), "-Verbosity", "detailed", "-Source", params.getRepoUrlStr()};
        NuGetCmdOutput nuGetCmdOutput;
        synchronized (params.getRepoId().intern()) {
            nuGetCmdOutput = processRunner.execute(command, params.isHttp());
        }
        if (nuGetCmdOutput != null && nuGetCmdOutput.isSuccess()) {
            nuGetCmdOutput.validateAndParse(params);
            return nuGetCmdOutput.getPackageRevision(params.getRepoUrl());
        }
        LOGGER.info(nuGetCmdOutput.getErrorDetail());
        throw new RuntimeException(getErrorMessage(nuGetCmdOutput.getErrorSummary()));
    }

    private String getErrorMessage(String message) {
        return format("Error while querying repository with path '%s' and package spec '%s'. %s", params.getRepoUrlStr(), params.getPackageSpec(), message);
    }

}
