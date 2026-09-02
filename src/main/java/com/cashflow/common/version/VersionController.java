package com.cashflow.common.version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.SpringVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    @Value("${spring.application.name:CashFlow Microservice}")
    private String applicationName;

    @Value("${info.app.version:1.0.0}")
    private String defaultAppVersion;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @GetMapping("/version")
    public ResponseEntity<SystemVersionResponse> getVersionInfo() {
        String name = (buildProperties != null && buildProperties.getName() != null)
                ? buildProperties.getName()
                : (applicationName != null ? applicationName : "CashFlow Microservice");

        String version = (buildProperties != null && buildProperties.getVersion() != null)
                ? buildProperties.getVersion()
                : (defaultAppVersion != null ? defaultAppVersion : "1.0.0");

        String group = (buildProperties != null && buildProperties.getGroup() != null)
                ? buildProperties.getGroup()
                : "com.cashflow";

        String artifact = (buildProperties != null && buildProperties.getArtifact() != null)
                ? buildProperties.getArtifact()
                : name;

        SystemVersionResponse response = SystemVersionResponse.builder()
                .application(SystemVersionResponse.ApplicationInfo.builder()
                        .name(name)
                        .group(group)
                        .artifact(artifact)
                        .version(version)
                        .description("Centralized CashFlow Microservices Architecture Service")
                        .build())
                .runtime(SystemVersionResponse.RuntimeInfo.builder()
                        .javaVersion(System.getProperty("java.version"))
                        .javaVendor(System.getProperty("java.vendor"))
                        .javaVmName(System.getProperty("java.vm.name"))
                        .osName(System.getProperty("os.name"))
                        .osVersion(System.getProperty("os.version"))
                        .osArch(System.getProperty("os.arch"))
                        .build())
                .framework(SystemVersionResponse.FrameworkInfo.builder()
                        .springBootVersion(SpringBootVersion.getVersion() != null ? SpringBootVersion.getVersion() : "3.5.6")
                        .springFrameworkVersion(SpringVersion.getVersion() != null ? SpringVersion.getVersion() : "6.2.11")
                        .build())
                .dependencies(SystemVersionResponse.CommonDependenciesInfo.builder()
                        .cashflowCommonLibrary("1.0.0-SNAPSHOT")
                        .jjwt("0.12.6")
                        .springdocOpenApi("2.8.13")
                        .modelmapper("3.2.2")
                        .lombok("Managed by Spring Boot 3.5.6")
                        .jacoco("0.8.13")
                        .build())
                .plugins(SystemVersionResponse.BuildPluginInfo.builder()
                        .mavenCompilerPlugin("3.14.0 (Target Java 21)")
                        .springBootMavenPlugin("3.5.6")
                        .jacocoMavenPlugin("0.8.13")
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }
}
