package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryConfiguration;
import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import com.tw.go.plugin.nuget.config.RepoUrl;

import static com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NuGetConfig implements PackageRepositoryConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(NuGetConfig.class);
    public static final String PACKAGE_SPEC = "PACKAGE_SPEC";

    public PackageConfigurations getRepositoryConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(new PackageConfiguration(RepoUrl.REPO_URL).with(DISPLAY_NAME, "Package Source").with(DISPLAY_ORDER, 0));
        configurations.addConfiguration(new PackageConfiguration(RepoUrl.USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1));
        configurations.addConfiguration(new PackageConfiguration(RepoUrl.PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2));
        return configurations;
    }

    public PackageConfigurations getPackageConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(new PackageConfiguration(PACKAGE_SPEC).with(DISPLAY_NAME, "Package Spec").with(DISPLAY_ORDER, 0));
        return configurations;
    }

    public boolean isRepositoryConfigurationValid(PackageConfigurations repositoryConfigurations, Errors errors) {
        PackageConfiguration repositoryUrlConfiguration = repositoryConfigurations.get(RepoUrl.REPO_URL);
        PackageConfiguration username= repositoryConfigurations.get(RepoUrl.USERNAME);
        PackageConfiguration password= repositoryConfigurations.get(RepoUrl.PASSWORD);

        if (repositoryUrlConfiguration == null) {
            errors.addError(new ValidationError(RepoUrl.REPO_URL, "Repository url not specified"));
            return false;
        }
        String usernameValue = username == null? null: username.getValue();
        String passwordValue = password == null? null: password.getValue();

        RepoUrl.create(repositoryUrlConfiguration.getValue(), usernameValue, passwordValue).validate(errors);
        return !errors.hasErrors();
    }

    public boolean isPackageConfigurationValid(PackageConfigurations packageConfigurations, PackageConfigurations repositoryConfigurations, Errors errors) {
        PackageConfiguration artifactIdConfiguration = packageConfigurations.get(PACKAGE_SPEC);
        if (artifactIdConfiguration == null) {
            errors.addError(new ValidationError(PACKAGE_SPEC, "Package spec not specified"));
            return false;
        }
        String packageSpec = artifactIdConfiguration.getValue();
        if (packageSpec == null) {
            errors.addError(new ValidationError(PACKAGE_SPEC, "Package spec is null"));
            return false;
        }
        if (isBlank(packageSpec.trim())) {
            errors.addError(new ValidationError(PACKAGE_SPEC, "Package spec is empty"));
            return false;
        }
        if (packageSpec.contains("*") || packageSpec.contains("?")) {
            errors.addError(new ValidationError(PACKAGE_SPEC, String.format("Package spec [%s] is invalid", packageSpec)));
            return false;
        }
        return true;
    }

    public void testConnection(PackageConfigurations packageConfigurations, PackageConfigurations repositoryConfigurations) {
        try {
            new NuGetPoller().getLatestRevision(packageConfigurations, repositoryConfigurations);
        } catch (Exception e) {
            throw new RuntimeException("Test Connection failed: " + e.getMessage(), e);
        }
    }
}
