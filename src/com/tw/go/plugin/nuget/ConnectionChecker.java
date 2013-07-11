package com.tw.go.plugin.nuget;

public interface ConnectionChecker {
    void checkConnection(String path, Credentials credentials);
}
