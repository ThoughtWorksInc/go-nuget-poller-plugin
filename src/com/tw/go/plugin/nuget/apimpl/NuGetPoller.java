package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import com.tw.go.plugin.nuget.NuGetCmd;
import com.tw.go.plugin.nuget.NuGetCmdParams;
import com.tw.go.plugin.nuget.config.RepoUrl;

import static com.tw.go.plugin.nuget.apimpl.NuGetConfig.PACKAGE_ID;
import static com.tw.go.plugin.nuget.config.RepoUrl.PASSWORD;
import static com.tw.go.plugin.nuget.config.RepoUrl.REPO_URL;
import static com.tw.go.plugin.nuget.config.RepoUrl.USERNAME;

public class NuGetPoller implements PackageRepositoryPoller {
    private static Logger LOGGER = Logger.getLoggerFor(NuGetPoller.class);

    public PackageRevision getLatestRevision(PackageConfigurations packageConfig, PackageConfigurations repoConfig) {
        LOGGER.info(String.format("getLatestRevision called with packageId %s, for repo: %s",
                packageConfig.get(PACKAGE_ID).getValue(), repoConfig.get(REPO_URL).getValue()));
        validateConfig(repoConfig, packageConfig);
        RepoUrl repoUrl = getRepoUrl(repoConfig);
        PackageRevision packageRevision = executeNuGetCmd(repoUrl, packageConfig.get(PACKAGE_ID));
        LOGGER.info(String.format("getLatestRevision returning with %s, %s",
                packageRevision.getRevision(), packageRevision.getTimestamp()));
        return packageRevision;
    }

    private RepoUrl getRepoUrl(PackageConfigurations repoConfig) {
        return RepoUrl.create(
                    repoConfig.get(REPO_URL).getValue(),
                    new NuGetConfig().stringValueOf(repoConfig.get(USERNAME)),
                    new NuGetConfig().stringValueOf(repoConfig.get(PASSWORD)));
    }

    public PackageRevision latestModificationSince(PackageConfigurations packageConfig, PackageConfigurations repoConfig, PackageRevision previouslyKnownRevision) {
        LOGGER.info(String.format("latestModificationSince called with packageId %s, for repo: %s",
                packageConfig.get(PACKAGE_ID).getValue(), repoConfig.get(REPO_URL).getValue()));
        validateConfig(repoConfig, packageConfig);
        RepoUrl repoUrl = getRepoUrl(repoConfig);
        PackageRevision updatedPackage =
                executeNuGetCmd(repoUrl, packageConfig.get(PACKAGE_ID),
                    previouslyKnownRevision);
        if(updatedPackage == null){
            LOGGER.info(String.format("no modification since %s", previouslyKnownRevision.getRevision()));
            return null;
        }
        LOGGER.info(String.format("latestModificationSince returning with %s, %s",
                updatedPackage.getRevision(), updatedPackage.getTimestamp()));
        if (updatedPackage.getTimestamp().getTime() < previouslyKnownRevision.getTimestamp().getTime())
            LOGGER.warn(String.format("Updated Package %s published earlier (%s) than previous (%s, %s)",
                    updatedPackage.getRevision(), updatedPackage.getTimestamp(), previouslyKnownRevision.getRevision(), previouslyKnownRevision.getTimestamp()));
        return updatedPackage;
    }

    private PackageRevision executeNuGetCmd(RepoUrl repoUrl, PackageConfiguration packageId, PackageRevision lastKnownVersion) {
        return new NuGetCmd(new NuGetCmdParams(repoUrl, packageId.getValue(), lastKnownVersion)).execute();
    }

    private void validateConfig(PackageConfigurations repoConfig, PackageConfigurations packageConfig) {
        Errors errors = new Errors();
        new NuGetConfig().isRepositoryConfigurationValid(repoConfig, errors);
        new NuGetConfig().isPackageConfigurationValid(packageConfig, repoConfig, errors);
        if (errors.hasErrors()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : errors.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
        }
    }

    PackageRevision executeNuGetCmd(RepoUrl repoUrl, PackageConfiguration packageId) {
        return new NuGetCmd(new NuGetCmdParams(repoUrl, packageId.getValue())).execute();
    }
}
