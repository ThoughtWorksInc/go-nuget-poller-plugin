package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_LOCATION;
import static com.tw.go.plugin.nuget.NuGetPackage.PACKAGE_VERSION;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NuGetTest {
    @Test
    public void shouldReportLocationCorrectly() {
        PackageRevision result = new NuGet(new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null), "RouteMagic.Mvc", null, null, null, true)).poll();
        assertThat(result.getDataFor(PACKAGE_LOCATION), is("http://www.nuget.org/api/v2/package/RouteMagic.Mvc/1.2"));
    }


    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldFailIfNoPackagesFound() {
        expectedEx.expect(NuGetException.class);
        expectedEx.expectMessage("No such package found");
        new NuGet(new NuGetParams(RepoUrl.create("http://nuget.org/api/v2/", null, null), "Rou", null, null, null, true)).poll();
    }

    @Test
    public void shouldGetUpdateWhenLastVersionKnown() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("1Password-1.0.9.288", new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSION, "1.0.9.288");
        PackageRevision result = new NuGet(new NuGetParams(RepoUrl.create("http://chocolatey.org/api/v2", null, null), "1Password", null, null, lastKnownVersion, true)).poll();
        assertThat(result.getDataFor(PACKAGE_VERSION), is("1.0.9.333"));
    }

    @Test
    public void shouldReturnNullIfNoNewerRevision() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("1Password-10.0.9.332", new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSION, "10.0.9.332");
        NuGetParams params = new NuGetParams(RepoUrl.create("http://chocolatey.org/api/v2", null, null), "1Password", null, null, lastKnownVersion, true);
        assertNull(new NuGet(params).poll());

    }
}
