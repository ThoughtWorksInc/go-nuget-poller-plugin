package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import org.w3c.dom.NodeList;

import java.util.Date;

public class NuGetPackage {
    public static final String PACKAGE_DESCRIPTION = "DESCRIPTION";
    public static final String PACKAGE_LOCATION = "LOCATION";

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
        NodeList entries = feed.getEntries();
        if (entries.getLength() > 1)
            throw new RuntimeException(String.format("Multiple entries in feed for %s %s", pkgName, pkgVersion));

        String title = feed.getEntryTitle(entries);
        if (!pkgName.equals(title))
            throw new RuntimeException(String.format("Package name mismatch for %s: %s,", pkgName, title));
        NodeList properties = feed.getProperties();
        String version = feed.getProperty(properties, "Version");
        if (!pkgVersion.equals(version))
            throw new RuntimeException(String.format("Version mismatch for %s: %s, %s", pkgName, pkgVersion, version));
        Date publishedDate = feed.getPublishedDate(properties);
        return createPackageRevision(publishedDate, getPackageLabel(), feed.getAuthor(), feed.getPackageLocation());
    }

    PackageRevision createPackageRevision(Date publishedDate, String packageLabel, String author, String packageLocation) {
        PackageRevision result = new PackageRevision(packageLabel, publishedDate, author);
        result.addData(PACKAGE_LOCATION, packageLocation);
        result.addData(PACKAGE_DESCRIPTION, pkgDescription);
        return result;
    }

    public String getFilename() {
        return pkgName+"."+pkgVersion+".nupkg";
    }
}