package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

import java.util.Date;

public class NuGetPackage {
    public static final String PACKAGE_DESCRIPTION = "DESCRIPTION";
    public static final String PACKAGE_LOCATION = "LOCATION";
    public static final String PACKAGE_VERSIONONLY = "VERSIONONLY";

    public String getPackageName() {
        return pkgName;
    }

    private String pkgName;
    private String pkgVersion;
    private String pkgDescription;

    public NuGetPackage(String pkgName, String pkgVersion, String pkgDescription) {

        this.pkgName = pkgName;
        this.pkgVersion = pkgVersion;
        this.pkgDescription = pkgDescription;
    }

    public NuGetPackage(String pkgName, String pkgVersion) {
        this(pkgName, pkgVersion, "");
    }

    String getPackageLabel() {
        return String.format("%s-%s", pkgName, pkgVersion);
    }


    public PackageRevision getPackageRevision(NuGetFeedDocument feed) {
        rejectIfMultipleEntries(feed);
        String title = feed.getEntryTitle();
        if (!pkgName.equals(title))
            throw new RuntimeException(String.format("Package name mismatch for %s: %s,", pkgName, title));
        String version = feed.getPackageVersion();
        if (!pkgVersion.equals(version))
            throw new RuntimeException(String.format("Version mismatch for %s: %s, %s", pkgName, pkgVersion, version));
        return createPackageRevision(feed.getPublishedDate(), getPackageLabel(), feed.getAuthor(), feed.getPackageLocation(), feed.getPackageVersion());
    }

    private void rejectIfMultipleEntries(NuGetFeedDocument feed) {
        if (feed.getEntries().getLength() > 1)
            throw new RuntimeException(String.format("Multiple entries in feed for %s %s", pkgName, pkgVersion));
    }

    public PackageRevision createPackageRevision(Date publishedDate, String packageLabel, String author, String packageLocation, String packageVersion) {
        PackageRevision result = new PackageRevision(packageLabel, publishedDate, author);
        result.addData(PACKAGE_LOCATION, packageLocation);
        result.addData(PACKAGE_DESCRIPTION, pkgDescription);
        if(packageVersion != null) result.addData(NuGetPackage.PACKAGE_VERSIONONLY, packageVersion);
        return result;
    }

    public String getFilename() {
        return pkgName+"."+pkgVersion+".nupkg";
    }

    public String getPackageVersion() {
        return pkgVersion;
    }
}