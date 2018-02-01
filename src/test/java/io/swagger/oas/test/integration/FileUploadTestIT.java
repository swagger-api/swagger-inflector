package io.swagger.oas.test.integration;

import io.swagger.oas.test.client.ApiClient;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;

public class FileUploadTestIT {
    ApiClient client = new ApiClient();

    @Test
    public void verifyStringMetadata() throws Exception {
        String path = "/fileUpload";
        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("stringMetadata", "hello");
        Entity<?> formParams = Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA_TYPE);

        String str = client.invokeAPI(
                path,                           // path
                "POST",                         // method
                new HashMap<>(),  // query params
                null,                           // body
                new HashMap<>(),  // header params
                formParams,                     // form params
                "multipart/form-data",          // accept
                null,                           // content-type
                new String[0]);                 // auth names
        assertEquals(str, "hello");
    }

    @Test
    public void verifyFileUpload() throws Exception {
        File file = null;
        try {
            String path = "/fileUpload";
            file = File.createTempFile("inflector-test-", ".tmp");

            PrintWriter writer = new PrintWriter(file);
            writer.println("The first line");
            writer.println("The second line");
            writer.close();

            file = new File(file.getPath());

            final FileDataBodyPart filePart = new FileDataBodyPart("theFile", file);

            final MultiPart multipart = new FormDataMultiPart()
                    .field("stringMetadata", "bar")
                    .bodyPart(filePart);

            Entity<?> formParams = Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA_TYPE);

            String str = client.invokeAPI(
                    path,                           // path
                    "POST",                         // method
                    new HashMap<>(),  // query params
                    null,                           // body
                    new HashMap<>(),  // header params
                    formParams,                     // form params
                    "multipart/form-data",          // accept
                    null,                           // content-type
                    new String[0]);                 // auth names
            assertEquals(str, "bar: " + file.length());
        }
        finally {
            if(file != null) {
                file.delete();
            }
        }
    }

    @Test
    public void verifyMultipleMediaTypeFileUpload() throws Exception {
        File file = null;
        try {
            String path = "/multipleMediaType";
            file = File.createTempFile("inflector-test2-", ".tmp");

            PrintWriter writer = new PrintWriter(file);
            writer.println("The first line");
            writer.println("The second line");
            writer.close();

            file = new File(file.getPath());

            final FileDataBodyPart filePart = new FileDataBodyPart("file", file);

            final MultiPart multipart = new FormDataMultiPart()
                    .field("description", "foo")
                    .bodyPart(filePart);

            Entity<?> formParams = Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA_TYPE);

            String str = client.invokeAPI(
                    path,                           // path
                    "POST",                         // method
                    new HashMap<>(),  // query params
                    null,                           // body
                    new HashMap<>(),  // header params
                    formParams,                     // form params
                    "multipart/form-data",          // accept
                    "multipart/form-data",                           // content-type
                    new String[0]);                 // auth names
            assertEquals(str, "foo: " + file.length());
        }
        finally {
            if(file != null) {
                file.delete();
            }
        }
    }
}
