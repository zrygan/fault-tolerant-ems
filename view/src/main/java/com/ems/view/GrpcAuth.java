package com.ems.view;

import org.springframework.stereotype.Component;

import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import jakarta.servlet.http.HttpSession;

@Component
public class GrpcAuth {
    private static final Metadata.Key<String> AUTH =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    public <T extends AbstractStub<T>> T withToken(T stub, HttpSession session) {
        Metadata md = new Metadata();
        md.put(AUTH, "Bearer " + session.getAttribute("token"));
        return stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(md));
    }
}