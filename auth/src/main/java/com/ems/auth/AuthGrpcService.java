package com.ems.auth;

import com.ems.proto.auth.AuthServiceGrpc;
import com.ems.proto.auth.GetCurrentUserRequest;
import com.ems.proto.auth.LoginRequest;
import com.ems.proto.auth.LoginResponse;
import com.ems.proto.auth.LogoutRequest;
import com.ems.proto.auth.LogoutResponse;
import com.ems.proto.auth.Role;
import com.ems.proto.auth.UserInfo;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthGrpcService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    private UserInfo toInfo(UserAccount u) {
        return UserInfo.newBuilder()
                .setId(u.getId())
                .setEmail(u.getEmail())
                .setFullName(u.getFullName())
                .setRole(Role.valueOf("ROLE_" + u.getRole().toUpperCase()))
                .build();
    }

    @Override
    public void login(LoginRequest req, StreamObserver<LoginResponse> out) {
        var user = users.findByEmail(req.getEmail()).orElse(null);
        if (user == null || !encoder.matches(req.getPassword(), user.getPasswordHash())) {
            out.onError(Status.UNAUTHENTICATED.withDescription("Invalid email or password").asRuntimeException());
            return;
        }
        out.onNext(LoginResponse.newBuilder().setToken(jwt.create(user)).setUser(toInfo(user)).build());
        out.onCompleted();
    }

    @Override
    public void logout(LogoutRequest req, StreamObserver<LogoutResponse> out) {
        out.onNext(LogoutResponse.getDefaultInstance());
        out.onCompleted();
    }

    @Override
    public void getUser(GetCurrentUserRequest req, StreamObserver<UserInfo> out) {
        Long id = JwtServerInterceptor.USER_ID.get();
        var user = id == null ? null : users.findById(id).orElse(null);
        if (user == null) {
            out.onError(Status.UNAUTHENTICATED.asRuntimeException());
            return;
        }
        out.onNext(toInfo(user));
        out.onCompleted();
    }
}