package com.ems.courses;

import io.grpc.*;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
public class JwtServerInterceptor implements ServerInterceptor {

    public static final Context.Key<Long> USER_ID = Context.key("userId");
    public static final Context.Key<String> ROLE = Context.key("role");

    private static final Metadata.Key<String> AUTH =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final JwtService jwt;

    public JwtServerInterceptor(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String service = call.getMethodDescriptor().getServiceName();
        if (service != null && service.startsWith("grpc.")) {
            return next.startCall(call, headers);
        }

        String header = headers.get(AUTH);
        if (header != null && header.startsWith("Bearer ")) {
            var caller = jwt.verify(header.substring(7));
            if (caller != null) {
                Context ctx = Context.current()
                        .withValue(USER_ID, caller.id())
                        .withValue(ROLE, caller.role());
                return Contexts.interceptCall(ctx, call, headers, next);
            }
        }

        call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid token"), new Metadata());
        return new ServerCall.Listener<>() {};
    }
}