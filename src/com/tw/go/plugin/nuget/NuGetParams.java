package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;

import static com.tw.go.plugin.nuget.config.NuGetPackageConfig.PACKAGE_VERSION;

public class NuGetParams {
    public static final String ANY = "ANY";
    private final String packageId;
    private final RepoUrl repoUrl;
    private String pollVersionFrom = ANY;
    private String pollVersionTo = ANY;
    private PackageRevision lastKnownVersion = null;
    private boolean includePreRelease = true;

    public NuGetParams(RepoUrl repoUrl, String packageId, String pollVersionFrom, String pollVersionTo, PackageRevision previouslyKnownRevision, boolean includePreReleaseVersions) {
        this.repoUrl = repoUrl;
        this.packageId = packageId;
        if (pollVersionFrom != null && !pollVersionFrom.trim().isEmpty()) this.pollVersionFrom = pollVersionFrom;
        if (pollVersionTo != null && !pollVersionTo.trim().isEmpty()) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
        this.includePreRelease = includePreReleaseVersions;
    }

    @Override
    public String toString() {
        return String.format("packageId:%s, repoUrl:%s, pollVersionFrom:%s, pollVersionTo:%s, lastKnownRevision:%s, includePreRelease:%s", packageId, repoUrl.getUrlStr(), pollVersionFrom, pollVersionTo, lastKnownVersion, includePreRelease);
    }

    public String getPackageId() {
        return packageId;
    }

    public RepoUrl getRepoUrl() {
        return repoUrl;
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    public String getLastKnownVersion() {
        if (lastKnownVersion == null) return null;
        return lastKnownVersion.getDataFor(PACKAGE_VERSION);
    }

    public boolean lowerBoundGiven() {
        return !ANY.equals(pollVersionFrom);
    }

    public boolean upperBoundGiven() {
        return !ANY.equals(pollVersionTo);
    }

    public String getQuery() {
        StringBuilder query = new StringBuilder();
        query.append(((HttpRepoURL) repoUrl).getUrlWithBasicAuth());
        query.append("GetUpdates()?");
        query.append(String.format("packageIds='%s'", getPackageId()));
        query.append(String.format("&versions='%s'", getEffectiveLowerBound()));
        query.append("&includePrerelease=").append(includePreRelease);
        query.append("&includeAllVersions=true");//has to be true, filter gets applied later
        if (upperBoundGiven()) {
            query.append("&$filter=Version%20lt%20'").append(pollVersionTo).append("'");
        }
        query.append("&$orderby=Version%20desc&$top=1");
        return query.toString();
    }

    private String getEffectiveLowerBound() {
        if (getLastKnownVersion() != null) return getLastKnownVersion();
        if (lowerBoundGiven()) return pollVersionFrom;
        return "0.0.1";
    }

}
