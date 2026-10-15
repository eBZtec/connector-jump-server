package br.tec.ebz.polygon.connector.jumpserver.api;

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
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.jakarta.rs.json.JacksonJsonProvider;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connection {
    private static final Log LOG = Log.getLog(Connection.class);
    private static final String AUTH_URL = "/api/v1/authentication/auth/";

    private JumpServerConfiguration configuration;

    private String token;

    public Connection(JumpServerConfiguration configuration) throws MalformedURLException {
        this.configuration = configuration;
        fetchToken();
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

    protected <T> T setupClient(Class<T> type) throws MalformedURLException {
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

    private void fetchToken() throws MalformedURLException {
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
        if (token == null) {
            throw new ConnectionFailedException("Could not authenticate, reason: token is null.");
        }

        LOG.ok("Token was retrieved successfully.");

    }

    private String getAccessToken() {
        return token;
    }

    protected void setConfiguration(JumpServerConfiguration configuration) { this.configuration = configuration; }
}
