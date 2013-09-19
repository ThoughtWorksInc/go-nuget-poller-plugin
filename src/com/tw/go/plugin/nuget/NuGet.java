package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

public class NuGet {
    private static Logger LOGGER = Logger.getLoggerFor(NuGet.class);
    private final NuGetParams params;

    public NuGet(NuGetParams params) {
        this.params = params;
    }

    public PackageRevision poll() {
        return pollByAPI();
    }

    private PackageRevision pollByAPI() {
        String url = params.getQuery();
        LOGGER.info(url);
        return new NuGetFeedDocument(new Feed(url).download()).getPackageRevision(params.isLastVersionKnown());
    }

}
