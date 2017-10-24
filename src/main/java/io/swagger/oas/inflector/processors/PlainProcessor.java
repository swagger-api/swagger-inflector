package io.swagger.oas.inflector.processors;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.oas.inflector.converters.ConversionException;
import io.swagger.oas.inflector.validators.ValidationError;
import io.swagger.oas.inflector.validators.ValidationMessage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PlainProcessor implements EntityProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryProcessor.class);
    private static List<MediaType> SUPPORTED_TYPES = new ArrayList<>();

    static {
        SUPPORTED_TYPES.add(MediaType.TEXT_XML_TYPE);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return new ArrayList(SUPPORTED_TYPES);
    }

    @Override
    public void enableType(MediaType type) {
        MediaType t = type;
        if(t != null) {
            if(!SUPPORTED_TYPES.contains(t)) {
                SUPPORTED_TYPES.add(type);
            }
        }
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return SUPPORTED_TYPES.contains(mediaType);
    }

    @Override
    public Object process(MediaType mediaType, InputStream entityStream, Class<?> cls) throws ConversionException {
        try {
            return IOUtils.toByteArray(entityStream);
        } catch (IOException e) {
            LOGGER.trace("unable to extract entity from content-type `" + mediaType + "` to byte[]", e);
            throw new ConversionException()
                    .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message("unable to convert input to " + cls.getCanonicalName()));
        }
    }

    @Override
    public Object process(MediaType mediaType, InputStream entityStream, JavaType javaType) {
        try {
            return IOUtils.toByteArray(entityStream);
        } catch (IOException e) {
            LOGGER.error("unable to extract entity from content-type `" + mediaType + "` to byte[]", e);
        }
        return null;
    }
}
