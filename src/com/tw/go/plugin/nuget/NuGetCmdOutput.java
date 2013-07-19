package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.config.RepoUrl;
import com.tw.go.plugin.util.ListUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

public class NuGetCmdOutput {
    private static Logger LOGGER = Logger.getLoggerFor(NuGetCmdOutput.class);

    public static final String URL_PREFIX = "GET ";
    public static final Date MIN_DATE = new Date(0L);
    private int returnCode;
    protected List<String> stdOut;
    private List<String> stdErr;
    private boolean http;

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

    protected boolean noPackagesFound() {
        return "No packages found.".equals(stdOut.get(1).trim());
    }

    private String searchUrl;
    private List<String> otherPackages = new ArrayList<String>();
    private boolean moreThanOnePackage;
    private NuGetPackage nugetPkg;

    public void validateAndParse(NuGetCmdParams params) {
        if (isStdOutEmpty())
            throw new RuntimeException("Output is empty");
        http = params.isHttp();
        if (http && !startsWithGET())
            throw new RuntimeException("Unrecognized output format. Expected GET <search-url> but was " + getStdOut().get(0));
        if (noPackagesFound())
            throw new RuntimeException(String.format("No package with spec %s found in source %s", params.getPackageSpec(), params.getRepoUrlStr()));
        parse();
        if (moreThanOnePackage)
            throw new RuntimeException(String.format("Given PACKAGE_SPEC (%s) resolves to more than one package on the repository: %s, %s",
                    params.getPackageSpec(), nugetPkg.getPackageName(), ListUtil.join(otherPackages)));
    }

    private void parse() {
        if (http) searchUrl = stdOut.get(0).trim().substring(URL_PREFIX.length());
        nugetPkg = new NuGetPackage(getPackageTitle(), getPackageVersion(), getFirstLineOfDescription());
        List<Integer> pkgBoundaries = new ArrayList<Integer>();
        for (int i = 0; i < stdOut.size(); i++) {
            String line = stdOut.get(i);
            if (line.trim().isEmpty()) { //assuming no empty lines in description
                pkgBoundaries.add(i);
            }
            if (i > 0 && http && line.trim().startsWith(URL_PREFIX)) {
                String message = String.format("Found more than one line starting with GET\nline0:\nGET %s\nline%s:\n%s", searchUrl,i, line);
                LOGGER.warn(message);
                throw new RuntimeException(message);
            }
        }
        if (pkgBoundaries.size() > 1) moreThanOnePackage = true;
        for (Integer lineNumber : pkgBoundaries) {
            if (lineNumber + 1 < stdOut.size()) {
                otherPackages.add(stdOut.get(lineNumber + 1));//should be fine for file and http
            }
        }
    }

    protected String getFirstLineOfDescription() {
        return stdOut.get(3).trim();
    }

    protected String getPackageVersion() {
        return stdOut.get(2).trim();
    }

    protected String getPackageTitle() {
        return stdOut.get(1).trim();
    }

    public PackageRevision getPackageRevision(RepoUrl repoUrl) {
        if (http) return nugetPkg.getPackageRevision(getFeedDocument());
        String separator = repoUrl.getSeparator();
        return nugetPkg.createPackageRevision(MIN_DATE, nugetPkg.getPackageLabel(), "unknown", repoUrl.forDisplay() + separator + nugetPkg.getFilename(), nugetPkg.getPackageVersion());
    }

    private NuGetFeedDocument getFeedDocument() {
        return new NuGetFeedDocument(new Feed(searchUrl).download());
    }

    boolean isSuccess() {
        return isZeroReturnCode() && hasOutput() && !hasErrors();
    }
}
