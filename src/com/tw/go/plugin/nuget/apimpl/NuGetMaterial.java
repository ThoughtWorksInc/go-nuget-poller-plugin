package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryMaterial;

@Extension
public class NuGetMaterial implements PackageRepositoryMaterial {

    public NuGetConfig getConfig() {
        return new NuGetConfig();
    }

    public NuGetPoller getPoller() {
        return new NuGetPoller();
    }
}
