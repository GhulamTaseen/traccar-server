package org.traccar.api.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Path("/xml")
public class XmlResource {

    private static final String FILE_PATH = "C:\\Program Files\\Traccar\\conf\\traccar.xml";

    @GET
    @Produces(MediaType.TEXT_XML)
    public String getXmlFile() throws IOException {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return "File not found";
        }

        return Files.readString(file.toPath());
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String editFile(String content) throws IOException {

        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return "File not found";
        }

        Files.writeString(file.toPath(), content, StandardOpenOption.TRUNCATE_EXISTING);

        return "File updated successfully";

    }
}

