package org.traccar.api.resource;

import jakarta.inject.Inject;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.traccar.config.Config;

@Path("xml")
public class Xml {

    @Inject
    public Config config;

    @Inject
    public Xml(Config config) {
        this.config = config;
    }
    private File getConfigFile() {
        return new File(config.getString("config.file", "./conf/traccar.xml"));
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String xmlData() {

        File file = getConfigFile();

        if (!file.exists()) {
            throw new WebApplicationException("Config file not found", 404);
        }

        try {
            return Files.readString(file.toPath());
        } catch (Exception e) {
            throw new WebApplicationException("Failed to read config", 500);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML,
               MediaType.TEXT_PLAIN,
               MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public String sendXml(String content) {

        if (content == null  || content.isEmpty()) {
            throw new WebApplicationException("Empty XML", 400);
        }

        File file = getConfigFile();

        try {
            Files.writeString(
                    file.toPath(),
                    content,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new WebApplicationException("Failed to save config", 500);
        }

        return "Config saved successfully (restart required)";
    }
}
