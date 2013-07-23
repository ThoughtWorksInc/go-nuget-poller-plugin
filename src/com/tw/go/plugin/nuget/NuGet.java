package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.exe.NuGetCmdOutput;
import com.tw.go.plugin.nuget.exe.ProcessRunner;

import static java.lang.String.format;

public class NuGet {
    private final ProcessRunner processRunner;
    private static Logger LOGGER = Logger.getLoggerFor(NuGet.class);
    private final NuGetParams params;

    public NuGet(NuGetParams params) {
        this(new ProcessRunner(), params);
    }

    //for tests
    public NuGet(ProcessRunner processRunner, NuGetParams params) {
        this.processRunner = processRunner;
        this.params = params;
    }

    public PackageRevision execute() {
        if (!params.isHttp()) return nugetexe();
        try {
            return nugetApi();
        } catch (NuGetException apiFail) {
            throw apiFail;
        } catch (RuntimeException ex) {
            return nugetexe();
        }
    }

    private PackageRevision nugetexe() {
        if(params.lowerBoundGiven())
            throw new RuntimeException(String.format("Polling older version (%s) not supported via nuget.exe", params.getPackageAndVersion()));
        String[] command = {"nuget", "list", params.getPrefixedPackageId(),
                "-Verbosity", "detailed", "-Source", params.getRepoUrlStr()};
        NuGetCmdOutput nuGetCmdOutput;
        synchronized (params.getRepoId().intern()) {
            nuGetCmdOutput = processRunner.execute(command, params.isHttp());
        }
        if (nuGetCmdOutput.isSuccess()) {
            nuGetCmdOutput.validateAndParse(params);
            return nuGetCmdOutput.getPackageRevision(params.getRepoUrl());
        }
        LOGGER.info(nuGetCmdOutput.getErrorDetail());
        throw new RuntimeException(getErrorMessage(nuGetCmdOutput.getErrorSummary()));
    }

    private PackageRevision nugetApi() {
        String url = params.getQuery();
        LOGGER.info(url);
        return new NuGetFeedDocument(new Feed(url).download()).getPackageRevision(params.isLastVersionKnown());
    }

    private String getErrorMessage(String message) {
        return format("Error while querying repository with path '%s' and packageId '%s'. %s",
                params.getRepoUrlStr(), params.getPackageId(), message);
    }

}
