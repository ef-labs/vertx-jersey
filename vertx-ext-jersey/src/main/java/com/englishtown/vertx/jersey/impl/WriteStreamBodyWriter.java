package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.WriteStreamOutput;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Jersey {@link javax.ws.rs.ext.MessageBodyWriter} for {@link com.englishtown.vertx.jersey.WriteStreamOutput}
 */
public class WriteStreamBodyWriter implements MessageBodyWriter<WriteStreamOutput> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return WriteStreamOutput.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(WriteStreamOutput writeStreamOutput, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(WriteStreamOutput writeStreamOutput, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        // Do nothing
    }
}
