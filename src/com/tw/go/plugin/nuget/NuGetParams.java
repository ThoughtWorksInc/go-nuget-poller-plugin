package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_VERSIONONLY;

public class NuGetParams {
    public static final String LATEST = "LATEST";
    private final String packageId;
    private final RepoUrl repoUrl;
    private String pollVersionFrom = LATEST;
    private String pollVersionTo = LATEST;
    private PackageRevision lastKnownVersion = null;
    private boolean includePreRelease = false;//TODO: surface this option

    public NuGetParams(RepoUrl repoUrl, String packageId, String pollVersionFrom, String pollVersionTo, PackageRevision previouslyKnownRevision) {
        this.repoUrl = repoUrl;
        this.packageId = packageId;
        if(pollVersionFrom != null) this.pollVersionFrom = pollVersionFrom;
        if(pollVersionTo != null) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
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
        return lastKnownVersion.getDataFor(PACKAGE_VERSIONONLY);
    }

    public boolean lowerBoundGiven() {
        return !LATEST.equals(pollVersionFrom);
    }

    public boolean upperBoundGiven() {
        return !LATEST.equals(pollVersionTo);
    }

    public String getPackageAndVersion() {
        return packageId + "." + pollVersionFrom;
    }

    public String getQuery() {
        return isLastVersionKnown() ? getQuery(true) : getQuery(false);
    }

    private String getQuery(boolean getUpdate) {
        StringBuilder query = new StringBuilder();
        query.append(getRepoUrlStrWithTrailingSlash());
        if (getUpdate) {
            query.append("GetUpdates()?");
            query.append(String.format("packageIds='%s'", getPackageId()));
            query.append(String.format("&versions='%s'", getLastKnownVersion()));
            query.append("&includePrerelease=").append(includePreRelease);
            if (upperBoundGiven()) {
                query.append("&includeAllVersions=true");
                query.append("&$filter=Version%20lt%20'").append(pollVersionTo).append("'&$orderby=Version%20desc&$top=1");
            } else {
                query.append("&includeAllVersions=false");
            }
        } else {
            query.append("FindPackagesById()?");
            query.append(String.format("id='%s'&", getPackageId()));
            if (lowerBoundGiven() && upperBoundGiven()) {
                query.append("$filter=Version%20ge%20'").append(pollVersionFrom).
                        append("'%20and%20Version%20lt%20'").append(pollVersionTo).
                        append("'&$orderby=Version%20desc&$top=1");
            }else if (lowerBoundGiven()) {
                query.append("$filter=Version%20ge%20'").append(pollVersionFrom).append("'&$orderby=Version%20desc&$top=1");
            }else if(upperBoundGiven()){
                query.append("$filter=Version%20lt%20'").append(pollVersionTo).append("'&$orderby=Version%20desc&$top=1");
            }else {
                query.append("$filter=IsLatestVersion");
            }
        }
        return query.toString();
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
}
