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
    public static final PackageConfiguration REPO_CONFIG_REPO_URL =
            new PackageConfiguration(RepoUrl.REPO_URL).with(DISPLAY_NAME, "Package Source").with(DISPLAY_ORDER, 0);
    public static final PackageConfiguration REPO_CONFIG_USERNAME =
            new PackageConfiguration(RepoUrl.USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1);
    public static final PackageConfiguration REPO_CONFIG_PASSWORD =
            new PackageConfiguration(RepoUrl.PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2);
    public static final String PACKAGE_SPEC = "PACKAGE_SPEC";
    public static final PackageConfiguration PKG_CONFIG_PACKAGE_SPEC =
            new PackageConfiguration(PACKAGE_SPEC).with(DISPLAY_NAME, "Package Spec").with(DISPLAY_ORDER, 0);

    public PackageConfigurations getRepositoryConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(REPO_CONFIG_REPO_URL);
        configurations.addConfiguration(REPO_CONFIG_USERNAME);
        configurations.addConfiguration(REPO_CONFIG_PASSWORD);
        return configurations;
    }

    public PackageConfigurations getPackageConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(PKG_CONFIG_PACKAGE_SPEC);
        return configurations;
    }

    public boolean isRepositoryConfigurationValid(PackageConfigurations repoConfigs, Errors errors) {

        PackageConfiguration repoUrlConfig = repoConfigs.get(RepoUrl.REPO_URL);
        if (repoUrlConfig == null) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            errors.addError(new ValidationError(RepoUrl.REPO_URL, message));
            return false;
        }

        RepoUrl.create(
                repoUrlConfig.getValue(),
                stringValueOf(repoConfigs.get(RepoUrl.USERNAME)),
                stringValueOf(repoConfigs.get(RepoUrl.PASSWORD))).validate(errors);
        return !errors.hasErrors();
    }

    public String stringValueOf(PackageConfiguration packageConfiguration) {
        if (packageConfiguration == null) return null;
        return packageConfiguration.getValue();
    }

    public boolean isPackageConfigurationValid(PackageConfigurations packageConfig, PackageConfigurations repoConfig, Errors errors) {
        PackageConfiguration packageSpecConfig = packageConfig.get(PACKAGE_SPEC);
        if (packageSpecConfig == null) {
            String message = "Package spec not specified";
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_SPEC, message));
            return false;
        }
        String packageSpec = packageSpecConfig.getValue();
        if (packageSpec == null) {
            String message = "Package spec is null";
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_SPEC, message));
            return false;
        }
        if (isBlank(packageSpec.trim())) {
            String message = "Package spec is empty";
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_SPEC, message));
            return false;
        }
        if (packageSpec.contains("*") || packageSpec.contains("?")) {
            String message = String.format("Package spec [%s] is invalid", packageSpec);
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_SPEC, message));
            return false;
        }
        return true;
    }

    public void testConnection(PackageConfigurations packageConfigurations, PackageConfigurations repositoryConfigurations) {
        try {
            new NuGetPoller().getLatestRevision(packageConfigurations, repositoryConfigurations);
        } catch (Exception e) {
            String message = "Test Connection failed: " + e.getMessage();
            LOGGER.warn(message);
            throw new RuntimeException(message, e);
        }
    }
}
