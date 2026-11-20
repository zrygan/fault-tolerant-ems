package com.ems.clearance;

import com.ems.proto.clearance.*;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ClearanceGrpcService extends ClearanceServiceGrpc.ClearanceServiceImplBase {
  private static final Set<String> VALID_HOLD_TYPES = Set.of(
      "incomplete_records",
      "disciplinary_hold",
      "library_hold",
      "financial_hold");

  private final ClearanceRepository clearances;

  public ClearanceGrpcService(ClearanceRepository clearances) {
    this.clearances = clearances;
  }

  private static boolean isAdmin() {
    return "ADMIN".equalsIgnoreCase(JwtServerInterceptor.ROLE.get());
  }

  private static boolean isStudent() {
    return "STUDENT".equalsIgnoreCase(JwtServerInterceptor.ROLE.get());
  }

  private static Long requireAdmin(StreamObserver<?> out) {
    Long callerId = JwtServerInterceptor.USER_ID.get();
    if (callerId == null || !isAdmin()) {
      out.onError(Status.PERMISSION_DENIED.asRuntimeException());
      return null;
    }
    return callerId;
  }

  private static Timestamp toTimestamp(OffsetDateTime odt) {
    var instant = odt.toInstant();
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  private static ClearanceInfo toProto(Clearance c) {
    var b = ClearanceInfo.newBuilder()
        .setId(c.getId())
        .setStudentId(c.getStudentId())
        .setAdminId(c.getAdminId())
        .setHoldType(c.getHoldType())
        .setPlacedOn(toTimestamp(c.getPlacedOn()));

    if (c.getResolvedOn() != null) {
      b.setResolvedBy(c.getAdminId());
      b.setResolvedOn(toTimestamp(c.getResolvedOn()));
    }
    return b.build();
  }

  @Override
  public void placeHold(PlaceHoldRequest req, StreamObserver<PlaceHoldResponse> out) {
    Long callerId = requireAdmin(out);
    if (callerId == null)
      return;

    if (!VALID_HOLD_TYPES.contains(req.getHoldType())) {
      out.onError(Status.INVALID_ARGUMENT.asRuntimeException());
      return;
    }

    if (!clearances.isStudent(req.getStudentId())) {
      out.onError(Status.INVALID_ARGUMENT.asRuntimeException());
      return;
    }

    try {
      var saved = clearances.save(new Clearance(req.getStudentId(), callerId, req.getHoldType()));
      out.onNext(PlaceHoldResponse.newBuilder()
          .setClearance(toProto(saved))
          .build());
      out.onCompleted();
    } catch (DataIntegrityViolationException e) {
      out.onError(Status.ALREADY_EXISTS.asRuntimeException());
    } catch (Exception e) {
      out.onError(Status.INTERNAL.asRuntimeException());
    }
  }

  @Override
  public void listStudentClearances(ListStudentClearancesRequest req,
      StreamObserver<ListStudentClearancesResponse> out) {
    Long callerId = JwtServerInterceptor.USER_ID.get();

    // ensure: any admin or student but their own id only
    if (!isAdmin() && !(isStudent() && callerId != null && callerId.equals(req.getStudentId()))) {
      out.onError(Status.PERMISSION_DENIED.asRuntimeException());
      return;
    }

    try {
      List<Clearance> rows = req.getUnresolvedOnly()
          ? clearances.findByStudentIdAndResolvedOnIsNull(req.getStudentId())
          : clearances.findByStudentId(req.getStudentId());

      var res = ListStudentClearancesResponse.newBuilder();
      rows.forEach(c -> res.addClearances(toProto(c)));
      out.onNext(res.build());
      out.onCompleted();
    } catch (Exception e) {
      out.onError(Status.INTERNAL.asRuntimeException());
    }
  }

  @Override
  public void listStudentClearancesByHoldType(
      ListStudentClearancesByHoldTypeRequest req,
      StreamObserver<ListStudentClearancesByHoldTypeResponse> out) {
    if (requireAdmin(out) == null)
      return;

    if (!VALID_HOLD_TYPES.contains(req.getHoldType())) {
      out.onError(Status.INVALID_ARGUMENT.asRuntimeException());
      return;
    }

    try {
      var res = ListStudentClearancesByHoldTypeResponse.newBuilder();
      clearances.findByHoldTypeAndResolvedOnIsNull(req.getHoldType())
          .forEach(c -> res.addClearances(toProto(c)));
      out.onNext(res.build());
      out.onCompleted();
    } catch (Exception e) {
      out.onError(Status.INTERNAL.asRuntimeException());
    }
  }

  @Override
  public void listStudentClearancesByPlacedBy(
      ListStudentClearancesByPlacedByRequest req,
      StreamObserver<ListStudentClearancesByPlacedByResponse> out) {
    if (requireAdmin(out) == null)
      return;

    try {
      var res = ListStudentClearancesByPlacedByResponse.newBuilder();
      clearances.findByAdminIdAndResolvedOnIsNull(req.getPlacedBy())
          .forEach(c -> res.addClearances(toProto(c)));
      out.onNext(res.build());
      out.onCompleted();
    } catch (Exception e) {
      out.onError(Status.INTERNAL.asRuntimeException());
    }
  }

  @Override
  public void resolveHold(ResolveHoldRequest req, StreamObserver<ResolveHoldResponse> out) {
    Long callerId = requireAdmin(out);
    if (callerId == null)
      return;

    var hold = clearances.findById(req.getClearanceId()).orElse(null);
    if (hold == null) {
      out.onError(Status.NOT_FOUND.asRuntimeException());
      return;
    }

    if (!hold.getAdminId().equals(callerId)) {
      out.onError(Status.PERMISSION_DENIED.asRuntimeException());
      return;
    }

    if (hold.getResolvedOn() != null) {
      out.onError(Status.FAILED_PRECONDITION.asRuntimeException());
      return;
    }

    try {
      hold.resolve();
      var saved = clearances.save(hold);
      out.onNext(ResolveHoldResponse.newBuilder()
          .setClearance(toProto(saved))
          .build());
      out.onCompleted();
    } catch (Exception e) {
      out.onError(Status.INTERNAL.asRuntimeException());
    }
  }
}
