NuGet Poller Plugin for Go
==================================

Introduction
------------
This is a [package material](http://www.thoughtworks.com/products/docs/go/13.3/help/package_material.html) plugin for [Go](http://www.thoughtworks.com/products/go-continuous-delivery). It is currently capable of polling [NuGet](http://www.nuget.org/) repositories running [API V2](http://chris.eldredge.io/blog/2013/02/25/fun-with-nuget-rest-api/).

The behaviour and capabilities of the plugin is determined to a significant extent by that of the package material extension point in Go. Be sure to read the package material documentation before using this plugin.

This is a pure Java plugin. It does not need nuget.exe. You may however require nuget.exe on the agents.

Installation
------------
Just drop [go-nuget-poller.jar](https://github.com/ThoughtWorksInc/go-nuget-poller-plugin/releases/download/v0.1.0/go-nuget-poller.jar) into plugins/external directory and restart Go. More details [here](http://www.thoughtworks.com/products/docs/go/13.3/help/plugin_user_guide.html)

Repository definition
---------------------
![Add a NuGet repository][1]

NuGet Server URL must be a valid http or https URL. For example, to add nuget.org as a repository, specify the URL as http://nuget.org/api/v2. The plugin will try to access URL$metadata to report successful connection. Basic authentication (user:password@host/path) is supported.

Package definition
------------------
Click check package to make sure the plugin understands what you are looking for. Note that the version constraints are ANDed if both are specified.

![Define a package as material for a pipeline][2]

Package Metadata
----------------
The following additional [Nuspec](http://docs.nuget.org/docs/reference/nuspec-reference) info is accessed by the plugin:

ProjectUrl, if available, is used to display a TrackBack link. This is handy when the package is published outside of Go and we need a way to trace back to the piece of automation infrastructure (e.g. Jenkins job) that published it.

ReleaseNotes, if available, its first line is displayed as a modification comment.

Author, if available is shown as Modified By.

Published Environment Variables
-------------------------------
The following information is made available as environment variables for tasks:

    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LABEL
    GO_REPO_<REPO-NAME>_<PACKAGE-NAME>_REPO_URL
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_PACKAGE_ID
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LOCATION

The LOCATION variable points to a downloadable url.

Downloading the Package
-----------------------
To download the package locally on the agent, we could write a [curl](http://curl.haxx.se/) (or wget) task like this:

                <exec command="cmd" >
                <arg>/c</arg>
                <arg>curl -o c:\path\mypkg.nupkg $GO_PACKAGE_REPONAME_PKGNAME_LOCATION</arg>
                </exec>

When the task executes on the agent, the environment variables get subsituted and the package gets downloaded.

Alternatively, we could choose to *nuget install* the package like [this](https://github.com/goteam/go-command-repo/blob/master/package/nuget/nuget-install.xml)

Notes
-----
This plugin will detect at max one package revision per minute (the default interval at which Go materials poll). If multiple versions of a package get published to a repo in the time interval between two polls, Go will only register the latest version in that interval.
	
[1]: img/nuget-repo.png  "Define NuGet Package Repository"
[2]: img/nuget-add-pkg.png  "Define package as material for a pipeline"
