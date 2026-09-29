/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.tec.ebz.polygon.connector.jumpserver;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import java.net.URL;

public class JumpServerConfiguration extends AbstractConfiguration {

    private static final Log LOG = Log.getLog(JumpServerConfiguration.class);

    private String baseUrl;
    private String username;
    private GuardedString password;
    private Boolean isTrustAllCertificates = false;
    private Boolean isDisableCNCheck = true;

    @ConfigurationProperty(
            displayMessageKey = "jumpserver.config.baseUrl",
            helpMessageKey = "jumpserver.config.baseUrl.help",
            required = true,
            order = 1
    )
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @ConfigurationProperty(
            displayMessageKey = "jumpserver.config.username",
            helpMessageKey = "jumpserver.config.username.help",
            required = true,
            order = 2
    )
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ConfigurationProperty(
            displayMessageKey = "jumpserver.config.password",
            helpMessageKey = "jumpserver.config.password.help",
            required = true,
            order = 3,
            confidential = true
    )
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(GuardedString password) {
        this.password = password;
    }

    @ConfigurationProperty(
            displayMessageKey = "jumpserver.config.trustAll",
            helpMessageKey = "jumpserver.config.trustAll.help",
            order = 4
    )
    public Boolean getIsTrustAllCertificates() {
        return isTrustAllCertificates;
    }

    public void setIsTrustAllCertificates(Boolean isTrustAllCertificates) {
        this.isTrustAllCertificates = isTrustAllCertificates;
    }

    @ConfigurationProperty(
            displayMessageKey = "jumpserver.config.isDisableCNCheck",
            helpMessageKey = "jumpserver.config.isDisableCNCheck.help",
            order = 5
    )
    public Boolean getDisableCNCheck() {
        return isDisableCNCheck;
    }

    public void setDisableCNCheck(Boolean disableCNCheck) {
        isDisableCNCheck = disableCNCheck;
    }

    @Override
    public void validate() {
        String errorMessage = null;

        if (baseUrl == null || baseUrl.isBlank()) {
            errorMessage = "Base URL é um campo obrigatório";
        } else {
            try {
                new URL(baseUrl).toURI();
            } catch (Exception e) {
                errorMessage = "Informe uma URL válida no campo \"Base URL\"";
            }
        }

        if (username == null || username.isBlank()) errorMessage = "Campo username é obrigatório.";

        if (password == null) errorMessage = "Campo da senha é obrigatório.";

        if (errorMessage != null) throw new ConfigurationException(errorMessage);
    }
}