package com.ems.view;

import com.ems.proto.auth.AuthServiceGrpc;
import com.ems.proto.courses.CourseServiceGrpc;
import com.ems.proto.grades.GradesServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {
    @Bean
    AuthServiceGrpc.AuthServiceBlockingStub authStub(GrpcChannelFactory channels) {
        return AuthServiceGrpc.newBlockingStub(channels.createChannel("auth"));
    }
    @Bean
    CourseServiceGrpc.CourseServiceBlockingStub coursesStub(GrpcChannelFactory channels) {
        return CourseServiceGrpc.newBlockingStub(channels.createChannel("courses"));
    }
    @Bean
    GradesServiceGrpc.GradesServiceBlockingStub gradesStub(GrpcChannelFactory channels) {
        return GradesServiceGrpc.newBlockingStub(channels.createChannel("grades"));
    }
}