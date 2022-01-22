package eu.pinske.playgroundhttp;

import static java.util.Arrays.asList;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;

@SpringBootApplication
public class PlaygroundHttpApplication {

    public static void main(String[] args) {
        if (args.length > 0) {
            System.setProperty("java.util.logging.config.file", "logging.properties");
            System.setProperty("javax.net.ssl.trustStore", "src/main/resources/keystore.p12");
            System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
            System.setProperty("javax.net.ssl.trustStorePassword", "password");

            Jackson2ObjectMapperBuilder json = Jackson2ObjectMapperBuilder.json();
            if ("fix".equals(args[0])) {
                json = json.featuresToDisable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            }
            RestTemplate restTemplate = new RestTemplate(asList(new MappingJackson2HttpMessageConverter(json.build())));

            // https://datatracker.ietf.org/doc/html/rfc7230#section-4.1
            restTemplate.getForObject("https://localhost:8443/api", JsonNode.class);
            restTemplate.getForObject("https://localhost:8443/api", JsonNode.class);
        } else {
            SpringApplication.run(PlaygroundHttpApplication.class, args);
        }
    }

    @RestController
    public static class RestService {
        @GetMapping(path = "/api")
        public void api(HttpServletResponse out) throws IOException {
            out.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String data = "abcdefghijklmnopqrstuvwxyz";
            out.getOutputStream().write(("{\"" + data + "\":\"" + data + "\"}").getBytes());
            out.flushBuffer();
        }
    }
}
