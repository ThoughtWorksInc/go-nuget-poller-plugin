package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Test;

import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_VERSION;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NuGetParamsTest {
    @Test
    public void shouldHandleUpperBound(){
        NuGetParams params = new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null),
                "RouteMagic", null, "1.2", null, true);
        assertThat(params.getQuery(),
                is("http://www.nuget.org/api/v2/GetUpdates()?packageIds='RouteMagic'&versions='0.0.1'&includePrerelease=true&includeAllVersions=true&$filter=Version%20lt%20'1.2'&$orderby=Version%20desc&$top=1"));
    }
    @Test
    public void shouldHandleLowerBound(){
        NuGetParams params = new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null),
                "RouteMagic", "1.3", null, null, true);
        assertThat(params.getQuery(),
                is("http://www.nuget.org/api/v2/GetUpdates()?packageIds='RouteMagic'&versions='1.3'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1"));
    }
    @Test
    public void shouldHandleLowerAndUpperBound(){
        NuGetParams params = new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null),
                "RouteMagic", "1.1.2", "1.4", null, true);
        assertThat(params.getQuery(),
                is("http://www.nuget.org/api/v2/GetUpdates()?packageIds='RouteMagic'&versions='1.1.2'&includePrerelease=true&includeAllVersions=true&$filter=Version%20lt%20'1.4'&$orderby=Version%20desc&$top=1"));
    }
    @Test
    public void shouldHandleUpperBoundDuringUpdate(){
        PackageRevision known = new PackageRevision("1.1.2",null,"abc");
        known.addData(PACKAGE_VERSION, "1.1.2");
//        PackageRevision result = new NuGet(new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null), "RouteMagic", null, "1.4", known)).poll();
//        assertThat(result.getDataFor(PACKAGE_LOCATION), is("file://d:/tmp/nuget-local-repo/RouteMagic.1.2.nupkg"));
        NuGetParams params = new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null),
                "RouteMagic", null, "1.4", known, false);
        assertThat(params.getQuery(),
                is("http://www.nuget.org/api/v2/GetUpdates()?packageIds='RouteMagic'&versions='1.1.2'&includePrerelease=false&includeAllVersions=true&$filter=Version%20lt%20'1.4'&$orderby=Version%20desc&$top=1"));
    }
    @Test
    public void shouldIgnoreLowerBoundDuringUpdate(){
        PackageRevision known = new PackageRevision("1.1.2",null,"abc");
        known.addData(PACKAGE_VERSION,"1.1.2");
        NuGetParams params = new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null),
                "RouteMagic", "1.0", null, known, true);
        assertThat(params.getQuery(),
                is("http://www.nuget.org/api/v2/GetUpdates()?packageIds='RouteMagic'&versions='1.1.2'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1"));
    }
}
