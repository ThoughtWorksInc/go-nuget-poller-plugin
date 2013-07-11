package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.ListUtil;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NuGetCmdOutput {
    private static final String PACKAGE_DESCRIPTION = "DESCRIPTION";
    public static final String PACKAGE_LOCATION = "LOCATION";
    private String searchUrl;
    private String pkgName;
    private String pkgVersion;
    private String pkgDescription;
    private List<String> otherPackages = new ArrayList<String>();
    private boolean moreThanOnePackage;

    public NuGetCmdOutput(NuGetCmdParams params, com.tw.go.plugin.nuget.ProcessOutput processOutput) {
        List<String> stdOut = processOutput.getStdOut();
        if(stdOut == null || stdOut.isEmpty())
            throw new RuntimeException("Output is empty");
        if(!stdOut.get(0).trim().startsWith("GET"))
            throw new RuntimeException("Unrecognized output format. Expected GET <search-url> but was "+stdOut.get(0));
        if("No packages found.".equals(stdOut.get(1)))
            throw new RuntimeException(String.format("No package with spec %s found in source %s", params.getPackageSpec(), params.getRepoUrl()));
        process(stdOut);
        if(moreThanOnePackage)
            throw new RuntimeException(String.format("Given PACKAGE_SPEC (%s) resolves to more than one package on the repository: %s", params.getPackageSpec(), ListUtil.join(otherPackages)));
    }

    private void process(List<String> stdOut) {
        searchUrl = stdOut.get(0).trim().substring(4);
        pkgName = stdOut.get(1).trim();
        pkgVersion = stdOut.get(2).trim();
        pkgDescription = stdOut.get(3).trim();
        List<Integer> pkgBoundaries = new ArrayList<Integer>();
        for (int i = 0; i < stdOut.size(); i++) {
            String line = stdOut.get(i);
            if (line.trim().isEmpty()){
                pkgBoundaries.add(i);
            }
            if (i>0 && line.trim().startsWith("GET ")) {
                break;
            }
        }
        if(pkgBoundaries.size() > 1)  moreThanOnePackage=true;
        for(Integer lineNumber : pkgBoundaries){
            if(lineNumber+1 < stdOut.size()){
                otherPackages.add(stdOut.get(lineNumber + 1));
            }
        }
    }

    public NuGetCmdOutput(String pkgName, String pkgVersion) {
        this.pkgName = pkgName;
        this.pkgVersion = pkgVersion;
        searchUrl = null;
        pkgDescription = null;
    }

    public PackageRevision getPackageRevision(NuGetFeedDocument feed) {
        NodeList entries = feed.getEntries();
        if(entries.getLength() > 1) throw new RuntimeException(String.format("Multiple entries in feed for %s %s", pkgName, pkgVersion));

        String title = feed.getEntryTitle(entries);
        if(!pkgName.equals(title))
            throw new RuntimeException(String.format("Package name mismatch for %s: %s,",pkgName, title));
        NodeList properties = feed.getProperties();
        String version = feed.getProperty(properties, "Version");
        if(!pkgVersion.equals(version))
            throw new RuntimeException(String.format("Version mismatch for %s: %s, %s",pkgName, pkgVersion, version));
        Date publishedDate = feed.getPublishedDate(properties);
        PackageRevision result = new PackageRevision(getPackageLabel(),publishedDate, feed.getAuthor());
        result.addData(PACKAGE_LOCATION, feed.getPackageLocation());
        result.addData(PACKAGE_DESCRIPTION, pkgDescription);
        return result;
    }

    String getPackageLabel() {
        return String.format("%s-%s",pkgName,pkgVersion);
    }

    public PackageRevision getPackageRevision() {
        return getPackageRevision(new NuGetFeedDocument(new Feed(searchUrl).download()));
    }
}
