package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.validation.Errors;
import com.thoughtworks.go.plugin.api.validation.ValidationError;
import com.tw.go.plugin.nuget.RepoUrl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.nuget.RepoUrl.REPO_URL;
import static com.tw.go.plugin.nuget.apimpl.NuGetConfig.PACKAGE_SPEC;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class NuGetConfigTest {
    private NuGetConfig nuGetConfig;

    @Before
    public void setUp() {
        nuGetConfig = new NuGetConfig();
    }

    @Test
    public void shouldGetRepositoryConfiguration() {
        PackageConfigurations configurations = nuGetConfig.getRepositoryConfiguration();
        assertThat(configurations.get(REPO_URL), is(notNullValue()));
        assertThat(configurations.get(REPO_URL).getOption(PackageConfiguration.SECURE), is(false));
        assertThat(configurations.get(REPO_URL).getOption(PackageConfiguration.REQUIRED), is(true));
        assertThat(configurations.get(REPO_URL).getOption(PackageConfiguration.DISPLAY_NAME), is("Package Source"));
        assertThat(configurations.get(REPO_URL).getOption(PackageConfiguration.DISPLAY_ORDER), is(0));
        assertThat(configurations.get(RepoUrl.USERNAME), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(PackageConfiguration.SECURE), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(PackageConfiguration.REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(PackageConfiguration.DISPLAY_NAME), is("UserName"));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(PackageConfiguration.DISPLAY_ORDER), is(1));
        assertThat(configurations.get(RepoUrl.PASSWORD), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(PackageConfiguration.SECURE), is(true));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(PackageConfiguration.REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(PackageConfiguration.DISPLAY_NAME), is("Password"));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(PackageConfiguration.DISPLAY_ORDER), is(2));
    }

    @Test
    public void shouldGetPackageConfiguration() {
        PackageConfigurations configurations = nuGetConfig.getPackageConfiguration();
        assertThat(configurations.get(PACKAGE_SPEC), is(notNullValue()));
        assertThat(configurations.get(NuGetConfig.PACKAGE_SPEC).getOption(PackageConfiguration.DISPLAY_NAME), is("Package Spec"));
        assertThat(configurations.get(NuGetConfig.PACKAGE_SPEC).getOption(PackageConfiguration.DISPLAY_ORDER), is(0));
    }

    @Test
    public void shouldCorrectlyCheckIfRepositoryConfigurationValid() {
        assertForRepositoryConfigurationErrors(new PackageConfigurations(), asList(new ValidationError(REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(configurations(REPO_URL, null), asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertForRepositoryConfigurationErrors(configurations(REPO_URL, ""), asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertForRepositoryConfigurationErrors(configurations(REPO_URL, "incorrectUrl"), asList(new ValidationError(REPO_URL, "Invalid URL : incorrectUrl")), false);
        assertForRepositoryConfigurationErrors(configurations(REPO_URL, "http://correct.com/url"), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldCorrectlyCheckIfPackageConfigurationValid() {
        assertForPackageConfigurationErrors(new PackageConfigurations(), asList(new ValidationError(PACKAGE_SPEC, "Package spec not specified")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_SPEC, null), asList(new ValidationError(PACKAGE_SPEC, "Package spec is null")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_SPEC, ""), asList(new ValidationError(PACKAGE_SPEC, "Package spec is empty")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_SPEC, "go-age?nt-*"), asList(new ValidationError(PACKAGE_SPEC, "Package spec [go-age?nt-*] is invalid")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_SPEC, "go-agent"), new ArrayList<ValidationError>(), true);
    }

    private void assertForRepositoryConfigurationErrors(PackageConfigurations repositoryConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        Errors errors = new Errors();
        boolean result = nuGetConfig.isRepositoryConfigurationValid(repositoryConfigurations, errors);
        assertThat(result, is(expectedValidationResult));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }

    private void assertForPackageConfigurationErrors(PackageConfigurations packageConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        Errors errors = new Errors();
        boolean result = nuGetConfig.isPackageConfigurationValid(packageConfigurations, new PackageConfigurations(), errors);
        assertThat(result, is(expectedValidationResult));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }

    private PackageConfigurations configurations(String key, String value) {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.add(new PackageConfiguration(key, value));
        return configurations;
    }
}
