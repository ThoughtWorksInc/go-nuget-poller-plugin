package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;

import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_VERSIONONLY;

public class NuGetParams {
    public static final String ANY = "ANY";
    private final String packageId;
    private final RepoUrl repoUrl;
    private String pollVersionFrom = ANY;
    private String pollVersionTo = ANY;
    private PackageRevision lastKnownVersion = null;
    private boolean includePreRelease = true;//TODO: surface this option

    public NuGetParams(RepoUrl repoUrl, String packageId, String pollVersionFrom, String pollVersionTo, PackageRevision previouslyKnownRevision, boolean includePreReleaseVersions) {
        this.repoUrl = repoUrl;
        this.packageId = packageId;
        if (pollVersionFrom != null && !pollVersionFrom.trim().isEmpty()) this.pollVersionFrom = pollVersionFrom;
        if (pollVersionTo != null && !pollVersionTo.trim().isEmpty()) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
        this.includePreRelease = includePreReleaseVersions;
    }

    public String getRepoId() {
        return repoUrl.getRepoId();
    }

    public String getPackageId() {
        return packageId;
    }

    public String getPrefixedPackageId() {
        if (!repoUrl.isHttp()) return packageId;
        return "Id:" + packageId;
    }

    public String getRepoUrlStr() {
        return repoUrl.forDisplay();
    }

    public RepoUrl getRepoUrl() {
        return repoUrl;
    }

    public boolean isHttp() {
        return repoUrl.isHttp();
    }

    public String getRepoUrlStrWithTrailingSlash() {
        if (repoUrl.forDisplay().endsWith("/")) return repoUrl.forDisplay();
        return repoUrl.forDisplay() + "/";
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    public String getLastKnownVersion() {
        if (lastKnownVersion == null) return null;
        return lastKnownVersion.getDataFor(PACKAGE_VERSIONONLY);
    }

    public boolean lowerBoundGiven() {
        return !ANY.equals(pollVersionFrom);
    }

    public boolean upperBoundGiven() {
        return !ANY.equals(pollVersionTo);
    }

    public String getPackageAndVersion() {
        if(eitherBoundGiven())
        return String.format("%s, %s to %s", packageId, displayVersion(pollVersionFrom), displayVersion(pollVersionTo));
        return packageId;
    }

    private String displayVersion(String version) {
        if(ANY.equals(version)) return ANY;
        return "V" + version;
    }

    public String getQuery() {
        StringBuilder query = new StringBuilder();
        query.append(getRepoUrlStrWithTrailingSlash());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NuGetParams that = (NuGetParams) o;

        if (includePreRelease != that.includePreRelease) return false;
        if (lastKnownVersion != null ? !lastKnownVersion.equals(that.lastKnownVersion) : that.lastKnownVersion != null)
            return false;
        if (!packageId.equals(that.packageId)) return false;
        if (pollVersionFrom != null ? !pollVersionFrom.equals(that.pollVersionFrom) : that.pollVersionFrom != null)
            return false;
        if (pollVersionTo != null ? !pollVersionTo.equals(that.pollVersionTo) : that.pollVersionTo != null)
            return false;
        if (!repoUrl.equals(that.repoUrl)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = packageId.hashCode();
        result = 31 * result + repoUrl.hashCode();
        result = 31 * result + (pollVersionFrom != null ? pollVersionFrom.hashCode() : 0);
        result = 31 * result + (pollVersionTo != null ? pollVersionTo.hashCode() : 0);
        result = 31 * result + (lastKnownVersion != null ? lastKnownVersion.hashCode() : 0);
        result = 31 * result + (includePreRelease ? 1 : 0);
        return result;
    }

    public boolean eitherBoundGiven() {
        return upperBoundGiven() || lowerBoundGiven();
    }

    public boolean shoudIncludePreRelease() {
        return includePreRelease;
    }
}
