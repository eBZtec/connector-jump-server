package br.tec.ebz.polygon.connector.jumpserver.api.connection;

import br.tec.ebz.polygon.connector.jumpserver.JumpServerConfiguration;
import com.evolveum.polygon.common.GuardedStringAccessor;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.jakarta.rs.json.JacksonJsonProvider;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIConnection {
    private static final Log LOG = Log.getLog(APIConnection.class);
    private static final String AUTH_URL = "/api/v1/authentication/auth/";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss Z");

    private JumpServerConfiguration configuration;

    private String token;
    private Date dateExpired;

    public APIConnection(JumpServerConfiguration configuration) {
        this.configuration = configuration;
    }

    private Date parseToDate(String inputDate) {
        if (inputDate == null) return null;
        try {
            OffsetDateTime odt = OffsetDateTime.parse(inputDate, FORMATTER);
            Instant instant = odt.toInstant();

            return Date.from(instant);
        } catch (Exception e) {
            throw new ConfigurationException("Could not parse token expired date, reason: " + e.getMessage());
        }
    }

    private void configureHttpConduit(HTTPConduit conduit) {
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setAllowChunking(false);
        conduit.setClient(policy);

        if (!configuration.getIsTrustAllCertificates()) {
            return;
        }

        TLSClientParameters tls = conduit.getTlsClientParameters();
        if (tls == null) {
            tls = new TLSClientParameters();
            conduit.setTlsClientParameters(tls);
        }

        tls.setTrustManagers(new TrustManager[] { new InsecureTrustManager() });
        tls.setDisableCNCheck(configuration.getDisableCNCheck());
    }

    private static final class InsecureTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) { }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) { }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    protected <T> T setupClient(Class<T> type) {
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setResourceClass(type);
        bean.setAddress(this.configuration.getBaseUrl());

        JacksonJsonProvider provider = new JacksonJsonProvider();
        JsonMapper mapper = new JsonMapper();
        provider.setMapper(mapper);

        bean.setProvider(provider);
        bean.getOutInterceptors().add(new BearerAuthInterceptor(getAccessToken()));

        T proxy = bean.create(type);

        ClientConfiguration config = WebClient.getConfig(proxy);
        configureHttpConduit(config.getHttpConduit());

        return proxy;
    }

    public void fetchToken() {
        LOG.ok("Starting JumpServer API authentication...");
        URI baseUri = URI.create(configuration.getBaseUrl());
        URI resolvedUri = baseUri.resolve(AUTH_URL);

        JsonMapper mapper = new JsonMapper();
        JacksonJsonProvider provider = new JacksonJsonProvider();
        provider.setMapper(mapper);

        WebClient client = WebClient.create(String.valueOf(resolvedUri), List.of(provider));
        configureHttpConduit(WebClient.getConfig(client).getHttpConduit());
        client.accept("application/json");
        client.type("application/json");

        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("username", configuration.getUsername());

        GuardedStringAccessor accessor = new GuardedStringAccessor();
        configuration.getPassword().access(accessor);
        authRequest.put("password", accessor.getClearString());

        Map<String, Object> responseBody;
        try (Response response = client.post(authRequest)) {

            LOG.info("Authentication HTTP status \"{0}\", response \"{1}\"", response.getStatus(), response.getStatusInfo().getReasonPhrase());

            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw new ConnectionFailedException("Could not not authenticate, reason: " + response);
            }

            responseBody = response.readEntity(new GenericType<>() {
            });
        }

        token = (String) responseBody.get("token");

        String dateExpiredStr = (String) responseBody.get("date_expired");
        dateExpired = parseToDate(dateExpiredStr);

        if (token == null) {
            throw new ConnectionFailedException("Could not authenticate, reason: token is null.");
        }

        LOG.ok("Token was retrieved successfully.");
        LOG.info("Token is valid until \"{0}\"", dateExpiredStr);

    }

    public Boolean isValidToken() {
        return !tokenIsExpired();
    }

    private String getAccessToken() {
        if (token == null || tokenIsExpired()) {
            LOG.warn("Token is invalid. Generating a new token...");
            fetchToken();
        }
        return token;
    }

    private Boolean tokenIsExpired() {
        if (dateExpired == null) return Boolean.TRUE;

        Instant expiryInstant = dateExpired.toInstant();
        Instant now = Instant.now();

        return now.isAfter(expiryInstant);

    }

    protected void setConfiguration(JumpServerConfiguration configuration) { this.configuration = configuration; }
}
