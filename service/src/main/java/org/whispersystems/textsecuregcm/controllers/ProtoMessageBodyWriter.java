package org.whispersystems.textsecuregcm.controllers;

import org.whispersystems.textsecuregcm.proto.Group;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Type;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

@Provider
@Produces("application/x-protobuf")
@Consumes("application/x-protobuf")
public class ProtoMessageBodyWriter implements MessageBodyWriter<Group>, MessageBodyReader<Group> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // TODO Auto-generated method stub
    return type == Group.class;
  }

  @Override
  public void writeTo(Group t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders, OutputStream out)
      throws IOException, WebApplicationException {
    Writer writer = new PrintWriter(out);
    t.writeTo(out);

  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // TODO Auto-generated method stub
    return type == Group.class;
  }

  @Override
  public Group readFrom(Class<Group> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, String> httpHeaders, InputStream in)
      throws IOException, WebApplicationException {
    return Group.parseFrom(in);
  }

}
