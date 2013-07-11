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
import com.tw.go.plugin.nuget.RepoUrl;

public class NuGetPoller implements PackageRepositoryPoller {
    private static Logger LOGGER = Logger.getLoggerFor(NuGetPoller.class);

    public PackageRevision getLatestRevision(PackageConfigurations packagePluginConfigurations, PackageConfigurations repositoryPluginConfigurations) {
        LOGGER.info(String.format("getLatestRevision called with spec %s, for repo: %s", packagePluginConfigurations.get(NuGetConfig.PACKAGE_SPEC).getValue(), repositoryPluginConfigurations.get(RepoUrl.REPO_URL).getValue()));
        validateData(repositoryPluginConfigurations, packagePluginConfigurations);
        PackageConfiguration repoUrlConfig = repositoryPluginConfigurations.get(RepoUrl.REPO_URL);
        PackageConfiguration username = repositoryPluginConfigurations.get(RepoUrl.USERNAME);
        PackageConfiguration password = repositoryPluginConfigurations.get(RepoUrl.PASSWORD);
        PackageConfiguration packageSpec = packagePluginConfigurations.get(NuGetConfig.PACKAGE_SPEC);
        String usernameValue = username == null ? null : username.getValue();
        String passwordValue = password == null ? null : password.getValue();

        RepoUrl repoUrl = new RepoUrl(repoUrlConfig.getValue(), usernameValue, passwordValue);
        repoUrl.checkConnection();
        PackageRevision packageRevision = executeNuGetCmd(repoUrl.getRepoId(), repoUrl, packageSpec);
        LOGGER.info(String.format("getLatestRevision returning with %s, %s", packageRevision.getRevision(), packageRevision.getTimestamp()));
        System.out.println(String.format("getLatestRevision returning with %s, %s", packageRevision.getRevision(), packageRevision.getTimestamp()));
        return packageRevision;
    }

    public PackageRevision latestModificationSince(PackageConfigurations packagePluginConfigurations, PackageConfigurations repositoryPluginConfigurations, PackageRevision previouslyKnownRevision) {
        PackageRevision latestRevision = getLatestRevision(packagePluginConfigurations, repositoryPluginConfigurations);

        if (latestRevision.getTimestamp().getTime() > previouslyKnownRevision.getTimestamp().getTime())
            return latestRevision;
        System.out.println(String.format("latestModificationSince returning null for previous %s, %s", previouslyKnownRevision.getRevision(), previouslyKnownRevision.getTimestamp()));
        return null;
    }

    private void validateData(PackageConfigurations repositoryConfigurations, PackageConfigurations packageConfigurations) {
        Errors errors = new Errors();
        new NuGetConfig().isRepositoryConfigurationValid(repositoryConfigurations, errors);
        new NuGetConfig().isPackageConfigurationValid(packageConfigurations, repositoryConfigurations, errors);
        if (errors.hasErrors()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : errors.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
        }
    }

    PackageRevision executeNuGetCmd(String repoId, RepoUrl url, PackageConfiguration packageSpec) {
        return new NuGetCmd(new NuGetCmdParams(repoId, url, packageSpec.getValue())).execute();
    }
}
