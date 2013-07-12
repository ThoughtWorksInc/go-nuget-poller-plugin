package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

import static java.lang.String.format;

public class NuGetCmd {
    static final String DELIMITER = "<=>";
    private final ProcessRunner processRunner;
    private static Logger LOGGER = Logger.getLoggerFor(NuGetCmd.class);
    private final NuGetCmdParams params;
    private NuGetFactory nuGetFactory;

    public NuGetCmd(NuGetCmdParams params) {
        this(new ProcessRunner(), params, new NuGetFactory());
    }

    //for tests
    public NuGetCmd(ProcessRunner processRunner, NuGetCmdParams params, NuGetFactory nuGetFactory) {
        this.processRunner = processRunner;
        this.params = params;
        this.nuGetFactory = nuGetFactory;
    }

    public PackageRevision execute() {
        String[] command = {"nuget","list",params.getPackageSpecWithIdFilter(),"-Verbosity","detailed","-Source",params.getRepoUrl()};
        ProcessOutput processOutput;
        synchronized (params.getRepoId().intern()) {
            processOutput = processRunner.execute(command);
        }
        if (isSuccessful(processOutput)) {
            return nuGetFactory.createNuGetCmdOutputInstance(params, processOutput).getPackageRevision();
        }
        LOGGER.info(processOutput.getErrorDetail());
        throw new RuntimeException(getErrorMessage(processOutput.getErrorSummary()));
    }

    private String getErrorMessage(String message) {
        return format("Error while querying repository with path '%s' and package spec '%s'. %s", params.getRepoUrl(), params.getPackageSpec(), message);
    }

    private boolean isSuccessful(ProcessOutput processOutput) {
        return processOutput != null && processOutput.isZeroReturnCode() && processOutput.hasOutput() && !processOutput.hasErrors();
    }

}
