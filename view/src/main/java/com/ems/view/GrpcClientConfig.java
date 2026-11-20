package com.ems.view;

import com.ems.proto.auth.AuthServiceGrpc;
import com.ems.proto.clearance.ClearanceServiceGrpc;
import com.ems.proto.courses.CourseServiceGrpc;
import com.ems.proto.grades.GradesServiceGrpc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {
    @Bean
    AuthServiceGrpc.AuthServiceBlockingStub authStub(
            GrpcChannelFactory channels,
            @Value("${AUTH_ADDRESS:localhost:9091}") String address) {
        return AuthServiceGrpc.newBlockingStub(channels.createChannel(address));
    }

    @Bean
    CourseServiceGrpc.CourseServiceBlockingStub coursesStub(
            GrpcChannelFactory channels,
            @Value("${COURSES_ADDRESS:localhost:9092}") String address) {
        return CourseServiceGrpc.newBlockingStub(channels.createChannel(address));
    }

    @Bean
    GradesServiceGrpc.GradesServiceBlockingStub gradesStub(
            GrpcChannelFactory channels,
            @Value("${GRADES_ADDRESS:localhost:9093}") String address) {
        return GradesServiceGrpc.newBlockingStub(channels.createChannel(address));

    @Bean
    ClearanceServiceGrpc.ClearanceServiceBlockingStub clearanceStub(
            GrpcChannelFactory channels,
            @Value("${GRADES_ADDRESS:localhost:9094}") String address) {
        return ClearanceServiceGrpc.newBlockingStub(channels.createChannel(address));
    }
}