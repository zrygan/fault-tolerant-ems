package com.ems.auth;

import io.grpc.*;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
public class JwtServerInterceptor implements ServerInterceptor {
    public static final Context.Key<Long> USER_ID = Context.key("userId");
    private static final Metadata.Key<String> AUTH =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    private final JwtService jwt;

    public JwtServerInterceptor(JwtService jwt) { this.jwt = jwt; }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String header = headers.get(AUTH);
        if (header != null && header.startsWith("Bearer ")) {
            Long id = jwt.verify(header.substring(7));
            if (id != null) {
                return Contexts.interceptCall(Context.current().withValue(USER_ID, id), call, headers, next);
            }
        }
        return next.startCall(call, headers);
    }
}
