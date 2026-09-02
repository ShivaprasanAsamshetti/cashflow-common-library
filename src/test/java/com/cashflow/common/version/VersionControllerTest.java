package com.cashflow.common.version;

import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class VersionControllerTest {

    @Test
    void getVersionInfoShouldReturnCompleteSystemVersionResponse() {
        VersionController controller = new VersionController();

        ResponseEntity<SystemVersionResponse> response = controller.getVersionInfo();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        SystemVersionResponse body = response.getBody();
        assertThat(body).isNotNull();

        // Application Info
        assertThat(body.getApplication()).isNotNull();
        assertThat(body.getApplication().getName()).isEqualTo("CashFlow Microservice");
        assertThat(body.getApplication().getGroup()).isEqualTo("com.cashflow");

        // Runtime Info
        assertThat(body.getRuntime()).isNotNull();
        assertThat(body.getRuntime().getJavaVersion()).isNotNull();
        assertThat(body.getRuntime().getJavaVendor()).isNotNull();
        assertThat(body.getRuntime().getOsName()).isNotNull();

        // Framework Info
        assertThat(body.getFramework()).isNotNull();
        assertThat(body.getFramework().getSpringBootVersion()).isEqualTo("3.5.6");
        assertThat(body.getFramework().getSpringFrameworkVersion()).isEqualTo("6.2.11");

        // Dependencies Info
        assertThat(body.getDependencies()).isNotNull();
        assertThat(body.getDependencies().getCashflowCommonLibrary()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(body.getDependencies().getJjwt()).isEqualTo("0.12.6");
        assertThat(body.getDependencies().getSpringdocOpenApi()).isEqualTo("2.8.13");
        assertThat(body.getDependencies().getModelmapper()).isEqualTo("3.2.2");

        // Build Plugins Info
        assertThat(body.getPlugins()).isNotNull();
        assertThat(body.getPlugins().getMavenCompilerPlugin()).contains("Target Java 21");
        assertThat(body.getPlugins().getSpringBootMavenPlugin()).isEqualTo("3.5.6");
        assertThat(body.getPlugins().getJacocoMavenPlugin()).isEqualTo("0.8.13");
    }

    @Test
    void getVersionInfoWithBuildPropertiesShouldReturnPropertiesFromBuildProperties() {
        Properties properties = new Properties();
        properties.setProperty("name", "custom-app");
        properties.setProperty("version", "2.0.0");
        properties.setProperty("group", "com.cashflow.custom");
        properties.setProperty("artifact", "custom-artifact");

        BuildProperties buildProperties = new BuildProperties(properties);
        VersionController controller = new VersionController();
        ReflectionTestUtils.setField(controller, "buildProperties", buildProperties);

        ResponseEntity<SystemVersionResponse> response = controller.getVersionInfo();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getApplication().getName()).isEqualTo("custom-app");
        assertThat(response.getBody().getApplication().getVersion()).isEqualTo("2.0.0");
        assertThat(response.getBody().getApplication().getGroup()).isEqualTo("com.cashflow.custom");
        assertThat(response.getBody().getApplication().getArtifact()).isEqualTo("custom-artifact");
    }
}

