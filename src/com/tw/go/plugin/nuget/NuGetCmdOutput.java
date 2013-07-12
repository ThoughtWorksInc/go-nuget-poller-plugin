package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

public class NuGetCmdOutput {
    private String searchUrl;
    private List<String> otherPackages = new ArrayList<String>();
    private boolean moreThanOnePackage;
    private NuGetPackage nugetPkg;

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
        nugetPkg = new NuGetPackage(stdOut.get(1).trim(), stdOut.get(2).trim(), stdOut.get(3).trim());
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

    public PackageRevision getPackageRevision() {
        return nugetPkg.getPackageRevision(getFeedDocument());
    }

    private NuGetFeedDocument getFeedDocument() {
        return new NuGetFeedDocument(new Feed(searchUrl).download());
    }
}
