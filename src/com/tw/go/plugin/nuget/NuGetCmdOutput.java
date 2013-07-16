package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

public class NuGetCmdOutput {
    private int returnCode;
    private List<String> stdOut;
    private List<String> stdErr;

    public NuGetCmdOutput(int returnCode, List<String> stdOut, List<String> stdErr) {
        this.returnCode = returnCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public List<String> getStdOut() {
        return stdOut;
    }

    public List<String> getStdErr() {
        return stdErr;
    }

    public String getErrorDetail() {
        if (hasErrors())
            return "Error Message: " + join(getStdErr(), "\n");
        return "";
    }

    public String getErrorSummary() {
        if (hasErrors())
            return "Error Message: " + stdErr.get(0);
        return "";
    }

    public boolean isZeroReturnCode() {
        return returnCode == 0;
    }

    public boolean hasOutput() {
        return stdOut != null && !stdOut.isEmpty();
    }

    public boolean hasErrors() {
        return stdErr != null && !stdErr.isEmpty();
    }

    @Override
    public String toString() {
        return "NuGetCmdOutput{" +
                "returnCode=" + returnCode +
                ", stdOut=" + stdOut +
                ", stdErr=" + stdErr +
                '}';
    }

    public boolean isStdOutEmpty() {
        return stdOut == null || stdOut.isEmpty();
    }

    boolean startsWithGET() {
        return stdOut.get(0).trim().startsWith("GET");
    }

    boolean noPackagesFound() {
        return "No packages found.".equals(stdOut.get(1));
    }

    private String searchUrl;
    private List<String> otherPackages = new ArrayList<String>();
    private boolean moreThanOnePackage;
    private NuGetPackage nugetPkg;

    public void validate(NuGetCmdParams params) {
        if (isStdOutEmpty())
            throw new RuntimeException("Output is empty");
        if (!startsWithGET())
            throw new RuntimeException("Unrecognized output format. Expected GET <search-url> but was " + getStdOut().get(0));
        if (noPackagesFound())
            throw new RuntimeException(String.format("No package with spec %s found in source %s", params.getPackageSpec(), params.getRepoUrl()));
        process(getStdOut());
        if (moreThanOnePackage)
            throw new RuntimeException(String.format("Given PACKAGE_SPEC (%s) resolves to more than one package on the repository: %s", params.getPackageSpec(), ListUtil.join(otherPackages)));
    }

    private void process(List<String> stdOut) {
        searchUrl = stdOut.get(0).trim().substring(4);
        nugetPkg = new NuGetPackage(stdOut.get(1).trim(), stdOut.get(2).trim(), stdOut.get(3).trim());
        List<Integer> pkgBoundaries = new ArrayList<Integer>();
        for (int i = 0; i < stdOut.size(); i++) {
            String line = stdOut.get(i);
            if (line.trim().isEmpty()) {
                pkgBoundaries.add(i);
            }
            if (i > 0 && line.trim().startsWith("GET ")) {
                break;//TODO: test reporting of multiple packages
            }
        }
        if (pkgBoundaries.size() > 1) moreThanOnePackage = true;
        for (Integer lineNumber : pkgBoundaries) {
            if (lineNumber + 1 < stdOut.size()) {
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

    boolean isSuccess() {
        return isZeroReturnCode() && hasOutput() && !hasErrors();
    }
}
