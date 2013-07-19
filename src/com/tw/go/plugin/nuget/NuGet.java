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
        try {
            if(!params.isHttp()) throw new RuntimeException("please goto catch. ugh");
            return findPackagesByIdApi();
        } catch (RuntimeException apiFail) {
            if(apiFail instanceof NuGetException) throw apiFail;
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
    }

    private PackageRevision findPackagesByIdApi() {
        String url = params.isLastVersionKnown() ? apiGetUpdates() : apiFindPackagesById();
        LOGGER.info(url);
        return new NuGetFeedDocument(new Feed(url).download()).getPackageRevision(params.isLastVersionKnown());
    }

    private String apiFindPackagesById() {
        return String.format("%sFindPackagesById()?$filter=IsLatestVersion&id='%s'",
                params.getRepoUrlStrWithTrailingSlash(),params.getPackageId());
    }

    private String apiGetUpdates() {
        return String.format("%sGetUpdates()?packageIds='%s'&versions='%s'&includePrerelease=true&includeAllVersions=false",
                params.getRepoUrlStrWithTrailingSlash(),params.getPackageId(), params.getLastKnownVersion());
    }


    private String getErrorMessage(String message) {
        return format("Error while querying repository with path '%s' and packageId '%s'. %s",
                params.getRepoUrlStr(), params.getPackageId(), message);
    }

}
