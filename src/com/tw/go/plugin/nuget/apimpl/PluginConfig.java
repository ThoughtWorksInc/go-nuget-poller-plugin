package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.config.Configuration;
import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
import com.tw.go.plugin.nuget.config.NuGetRepoConfig;
import com.tw.go.plugin.util.RepoUrl;

import java.util.Arrays;

import static com.thoughtworks.go.plugin.api.config.Property.*;
import static com.tw.go.plugin.nuget.config.NuGetPackageConfig.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PluginConfig implements PackageMaterialConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(PluginConfig.class);
    public static final Property REPO_CONFIG_REPO_URL =
            new PackageMaterialProperty(RepoUrl.REPO_URL).with(DISPLAY_NAME, "NuGet server API root").with(DISPLAY_ORDER, 0);
    public static final Property REPO_CONFIG_USERNAME =
            new PackageMaterialProperty(RepoUrl.USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1).with(PART_OF_IDENTITY, false);
    public static final Property REPO_CONFIG_PASSWORD =
            new PackageMaterialProperty(RepoUrl.PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, false);
    public static final Property PKG_CONFIG_PACKAGE_ID =
            new PackageMaterialProperty(PACKAGE_ID).with(DISPLAY_NAME, "Package Id").with(DISPLAY_ORDER, 0);
    public static final Property PKG_CONFIG_POLL_VERSION_FROM =
            new PackageMaterialProperty(POLL_VERSION_FROM).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll >=").with(DISPLAY_ORDER, 1).with(PART_OF_IDENTITY, false);
    public static final Property PKG_CONFIG_POLL_VERSION_TO =
            new PackageMaterialProperty(POLL_VERSION_TO).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll <").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, false);
    public static final Property PKG_CONFIG_INCLUDE_PRE_RELEASE =
            new PackageMaterialProperty(INCLUDE_PRE_RELEASE).with(REQUIRED, false).with(DISPLAY_NAME, "Include Prerelease? (yes/no, defaults to yes)").with(DISPLAY_ORDER, 3);

    public RepositoryConfiguration getRepositoryConfiguration() {
        RepositoryConfiguration configurations = new RepositoryConfiguration();
        configurations.add(REPO_CONFIG_REPO_URL);
        configurations.add(REPO_CONFIG_USERNAME);
        configurations.add(REPO_CONFIG_PASSWORD);
        return configurations;
    }

    public PackageConfiguration getPackageConfiguration() {
        PackageConfiguration configurations = new PackageConfiguration();
        configurations.add(PKG_CONFIG_PACKAGE_ID);
        configurations.add(PKG_CONFIG_POLL_VERSION_FROM);
        configurations.add(PKG_CONFIG_POLL_VERSION_TO);
        configurations.add(PKG_CONFIG_INCLUDE_PRE_RELEASE);
        return configurations;
    }

    public ValidationResult isRepositoryConfigurationValid(RepositoryConfiguration repoConfigs) {
        NuGetRepoConfig nuGetRepoConfig = new NuGetRepoConfig(repoConfigs);
        ValidationResult validationResult = new ValidationResult();
        if (nuGetRepoConfig.isRepoUrlMissing()) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            validationResult.addError(new ValidationError(RepoUrl.REPO_URL, message));
            return validationResult;
        }
        RepoUrl repoUrl = nuGetRepoConfig.getRepoUrl();
        if (!repoUrl.isHttp()) {
            String message = "Only http/https urls are supported";
            LOGGER.error(message);
            validationResult.addError(new ValidationError(RepoUrl.REPO_URL, message));
        }
        repoUrl.validate(validationResult);
        detectInvalidKeys(repoConfigs, validationResult, NuGetRepoConfig.getValidKeys());
        return validationResult;
    }

    private void detectInvalidKeys(Configuration config, ValidationResult errors, String[] validKeys) {
        for (Property property : config.list()) {
            boolean valid = false;
            for (String validKey : validKeys) {
                if (validKey.equals(property.getKey())) {
                    valid = true;
                    break;
                }
            }
            if (!valid)
                errors.addError(new ValidationError(String.format("Unsupported key: %s. Valid keys: %s", property.getKey(), Arrays.toString(validKeys))));
        }
    }

    public ValidationResult isPackageConfigurationValid(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
        NuGetPackageConfig nuGetPackageConfig = new NuGetPackageConfig(packageConfig);
        ValidationResult validationResult = new ValidationResult();
        if (nuGetPackageConfig.isPackageIdMissing()) {
            String message = "Package id not specified";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(PACKAGE_ID, message));
            return validationResult;
        }
        String packageId = nuGetPackageConfig.getPackageId();
        if (packageId == null) {
            String message = "Package id is null";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(PACKAGE_ID, message));
        }
        if (packageId != null && isBlank(packageId.trim())) {
            String message = "Package id is empty";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(PACKAGE_ID, message));
        }
        if (packageId != null && (packageId.contains("*") || packageId.contains("?"))) {
            String message = String.format("Package id [%s] is invalid", packageId);
            LOGGER.info(message);
            validationResult.addError(new ValidationError(PACKAGE_ID, message));
        }
        detectInvalidKeys(packageConfig, validationResult, NuGetPackageConfig.getValidKeys());
        NuGetRepoConfig nuGetRepoConfig = new NuGetRepoConfig(repoConfig);
        if (!nuGetRepoConfig.isHttp() && nuGetPackageConfig.hasBounds()) {
            String message = "Version constraints are only supported for NuGet feed servers";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(message));
        }
        return validationResult;
    }

}
