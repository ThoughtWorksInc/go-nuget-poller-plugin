package com.tw.go.plugin.nuget.apimpl;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProvider;

@Extension
public class NuGetProvider implements PackageMaterialProvider {

    public PluginConfig getConfig() {
        return new PluginConfig();
    }

    public NuGetPoller getPoller() {
        return new NuGetPoller();
    }
}
