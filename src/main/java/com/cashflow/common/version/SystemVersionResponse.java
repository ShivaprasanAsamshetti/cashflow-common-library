package com.cashflow.common.version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemVersionResponse {

    private ApplicationInfo application;
    private RuntimeInfo runtime;
    private FrameworkInfo framework;
    private CommonDependenciesInfo dependencies;
    private BuildPluginInfo plugins;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationInfo {
        private String name;
        private String group;
        private String artifact;
        private String version;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuntimeInfo {
        private String javaVersion;
        private String javaVendor;
        private String javaVmName;
        private String osName;
        private String osVersion;
        private String osArch;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrameworkInfo {
        private String springBootVersion;
        private String springFrameworkVersion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonDependenciesInfo {
        private String cashflowCommonLibrary;
        private String jjwt;
        private String springdocOpenApi;
        private String modelmapper;
        private String lombok;
        private String jacoco;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildPluginInfo {
        private String mavenCompilerPlugin;
        private String springBootMavenPlugin;
        private String jacocoMavenPlugin;
    }
}
