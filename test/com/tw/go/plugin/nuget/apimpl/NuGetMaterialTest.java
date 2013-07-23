package com.tw.go.plugin.nuget.apimpl;


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryPoller;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class NuGetMaterialTest {
    @Test
    public void shouldGetNugetRepositoryConfig() {
        NuGetMaterial repositoryMaterial = new NuGetMaterial();
        PackageRepositoryConfiguration repositoryConfiguration = repositoryMaterial.getConfig();
        assertThat(repositoryConfiguration, is(notNullValue()));
        assertThat(repositoryConfiguration, instanceOf(PluginConfig.class));
    }

    @Test
    public void shouldGetNugetRepositoryPoller() {
        NuGetMaterial repositoryMaterial = new NuGetMaterial();
        PackageRepositoryPoller poller = repositoryMaterial.getPoller();
        assertThat(poller, is(notNullValue()));
        assertThat(poller, instanceOf(NuGetPoller.class));
    }
}
