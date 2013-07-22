package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryConfiguration;
import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import com.tw.go.plugin.nuget.config.RepoUrl;

import static com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration.*;
import static com.tw.go.plugin.nuget.config.RepoUrl.PASSWORD;
import static com.tw.go.plugin.nuget.config.RepoUrl.REPO_URL;
import static com.tw.go.plugin.nuget.config.RepoUrl.USERNAME;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NuGetConfig implements PackageRepositoryConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(NuGetConfig.class);
    public static final PackageConfiguration REPO_CONFIG_REPO_URL =
            new PackageConfiguration(REPO_URL).with(DISPLAY_NAME, "Package Source").with(DISPLAY_ORDER, 0);
    public static final PackageConfiguration REPO_CONFIG_USERNAME =
            new PackageConfiguration(USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1);
    public static final PackageConfiguration REPO_CONFIG_PASSWORD =
            new PackageConfiguration(PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2);
    public static final String PACKAGE_ID = "PACKAGE_ID";
    public static final PackageConfiguration PKG_CONFIG_PACKAGE_ID =
            new PackageConfiguration(PACKAGE_ID).with(DISPLAY_NAME, "Package Id").with(DISPLAY_ORDER, 0);

    public PackageConfigurations getRepositoryConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(REPO_CONFIG_REPO_URL);
        configurations.addConfiguration(REPO_CONFIG_USERNAME);
        configurations.addConfiguration(REPO_CONFIG_PASSWORD);
        return configurations;
    }

    public PackageConfigurations getPackageConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(PKG_CONFIG_PACKAGE_ID);
        return configurations;
    }

    public boolean isRepositoryConfigurationValid(PackageConfigurations repoConfigs, Errors errors) {

        PackageConfiguration repoUrlConfig = repoConfigs.get(REPO_URL);
        if (repoUrlConfig == null) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            errors.addError(new ValidationError(REPO_URL, message));
            return false;
        }

        RepoUrl.create(
                repoUrlConfig.getValue(),
                stringValueOf(repoConfigs.get(USERNAME)),
                stringValueOf(repoConfigs.get(PASSWORD))).validate(errors);
        detectInvalidKeys(repoConfigs, errors, new String[]{REPO_URL, USERNAME, PASSWORD});
        return !errors.hasErrors();
    }

    private void detectInvalidKeys(PackageConfigurations configs, Errors errors, String[] validKeys){
        for(PackageConfiguration config : configs.list()){
            boolean valid = false;
            for(String validKey : validKeys){
                if(validKey.equals(config.getKey())) {
                    valid = true; break;
                }
            }
            if(!valid) errors.addError(new ValidationError("Unrecognized key: " + config.getKey()));
        }
    }

    public String stringValueOf(PackageConfiguration packageConfiguration) {
        if (packageConfiguration == null) return null;
        return packageConfiguration.getValue();
    }

    public boolean isPackageConfigurationValid(PackageConfigurations packageConfig, PackageConfigurations repoConfig, Errors errors) {
        PackageConfiguration packageIdConfig = packageConfig.get(PACKAGE_ID);
        if (packageIdConfig == null) {
            String message = "Package id not specified";
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_ID, message));
            return false;
        }
        String packageId = packageIdConfig.getValue();
        if (packageId == null) {
            String message = "Package id is null";
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_ID, message));
        }
        if (packageId != null && isBlank(packageId.trim())) {
            String message = "Package id is empty";
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_ID, message));
        }
        if (packageId != null && (packageId.contains("*") || packageId.contains("?"))) {
            String message = String.format("Package id [%s] is invalid", packageId);
            LOGGER.info(message);
            errors.addError(new ValidationError(PACKAGE_ID, message));
        }
        detectInvalidKeys(packageConfig, errors, new String[]{PACKAGE_ID});
        return !errors.hasErrors();
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
