package com.saessakmaeul.bitamin.consultations.controller;

import com.saessakmaeul.bitamin.consultations.Entity.SearchCondition;
import com.saessakmaeul.bitamin.consultations.dto.request.*;
import com.saessakmaeul.bitamin.consultations.dto.response.*;
import com.saessakmaeul.bitamin.consultations.service.ConsultationService;
import com.saessakmaeul.bitamin.util.JwtUtil;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ConsultationController {
    private final OpenVidu openVidu;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConsultationService consultationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> selectAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "100") int size,
                                       @RequestParam(value = "type") SearchCondition type) {

        List<SelectAllResponse> consultationList = consultationService.selectAll(page, size, type);

        if(consultationList == null) return ResponseEntity.status(404).body("다시 조회하세요");

        return ResponseEntity.ok(consultationList);
    }

    @PostMapping
    public ResponseEntity<?> registRoom(@RequestBody RegistRoomRequest registRoomRequest) throws OpenViduJavaClientException, OpenViduHttpException {

        Map<String,Object> params = new HashMap<>();
        params.put("customSessionId", UUID.randomUUID().toString());

        SessionProperties properties = SessionProperties.fromJson(params).build();

        Session session = openVidu.createSession(properties);

        registRoomRequest.setSessionId(session.getSessionId());

        RegistRoomResponse registRoomResponse = consultationService.registRoom(registRoomRequest);

        if(registRoomResponse == null) return ResponseEntity.status(404).body("방이 생성되지 않았습니다.");

        return ResponseEntity.status(201).body(registRoomResponse);

    }

    @PostMapping("/participants")
    public ResponseEntity<?> joinRoom(@RequestHeader(value = "Authorization", required = false) String tokenHeader,
                                      @RequestBody JoinRoomRequest joinRoomRequest) throws OpenViduJavaClientException, OpenViduHttpException {

        Map<String,Object> params = new HashMap<>();

        // 입장 가능한 세션인지 확인
        Session session = openVidu.getActiveSession(joinRoomRequest.getSessionId());

        if (session == null) return ResponseEntity.status(404).body("못 찾음");

        // DB에 저장
        Long memberId = jwtUtil.extractUserId(tokenHeader.substring(7));
        String memberNickname = jwtUtil.extractNickname(tokenHeader.substring(7));

        joinRoomRequest.setMemberId(memberId);
        joinRoomRequest.setMemberNickname(memberNickname);
        joinRoomRequest.setConsultationDate(joinRoomRequest.getStartTime().toLocalDate());

        JoinRoomResponse joinRoomResponse = consultationService.joinRoom(joinRoomRequest);

        if(joinRoomResponse == null) return ResponseEntity.status(404).body("방에 참여되지 않았습니다.");

        // connection 생성
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);

        joinRoomResponse.setToken(connection.getToken());

        return ResponseEntity.status(200).body(joinRoomResponse);
    }

    @PostMapping("/random-participants")
    public ResponseEntity<?> joinRandom(@RequestHeader(value = "Authorization", required = false) String tokenHeader,
                                        @RequestBody JoinRandomRequest joinRandomRequest) throws OpenViduJavaClientException, OpenViduHttpException{
        Map<String,Object> params = new HashMap<>();

        Map<String, Object> map = consultationService.findRandomSessionId(joinRandomRequest);

        if(map == null) return ResponseEntity.status(404).body("방 없음");

        joinRandomRequest.setSessionId(map.get("sessionId").toString());
        joinRandomRequest.setId(Long.parseLong(map.get("id").toString()));
        joinRandomRequest.setConsultationDate(((LocalDateTime)map.get("consultationDate")).toLocalDate());

        // 입장 가능한 세션인지 확인
        Session session = openVidu.getActiveSession(joinRandomRequest.getSessionId());

        if (session == null) return ResponseEntity.status(404).body("못 찾음");

        // DB에 저장
        Long memberId = jwtUtil.extractUserId(tokenHeader.substring(7));
        String memberNickname = jwtUtil.extractNickname(tokenHeader.substring(7));

        joinRandomRequest.setMemberId(memberId);
        joinRandomRequest.setMemberNickname(memberNickname);

        JoinRandomResponse joinRandomResponse = consultationService.joinRandom(joinRandomRequest);

        if(joinRandomResponse == null) return ResponseEntity.status(404).body("방에 참여되지 않았습니다.");

        // connection 생성
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);

        joinRandomResponse.setToken(connection.getToken());

        return ResponseEntity.status(200).body(joinRandomResponse);
    }

    @DeleteMapping("{consultationId}")
    public ResponseEntity<?> ExitRoomBeforeStart(@RequestHeader(value = "Authorization", required = false) String tokenHeader,
                                                 @PathVariable("consultationId") Long consultationId) {

        Long memberId = jwtUtil.extractUserId(tokenHeader.substring(7));

        ExitRoomBeforeStartRequest exitRoomBeforeStartRequest = new ExitRoomBeforeStartRequest(memberId, consultationId);
        exitRoomBeforeStartRequest.setMemberId(memberId);

        int result = consultationService.exitRoomBeforeStart(exitRoomBeforeStartRequest);

        if(result == 0) return ResponseEntity.status(404).body("퇴장하지 못 했습니다.");

        return ResponseEntity.status(200).body("정상적으로 퇴장 처리 되었습니다.");
    }

    @PatchMapping
    public ResponseEntity<?> ExitRoomAfterStart(@RequestHeader(value = "Authorization", required = false) String tokenHeader,
                                                @RequestBody ExitRoomAfterStartRequest exitRoomAfterStartRequest) {

        Long memberId = jwtUtil.extractUserId(tokenHeader.substring(7));
        exitRoomAfterStartRequest.setMemberId(memberId);

        int result = consultationService.exitRoomAfterStart(exitRoomAfterStartRequest);

        if(result == 0) return ResponseEntity.status(404).body("퇴장하지 못했습니다.");

        return ResponseEntity.status(200).body("정상적으로 퇴장 처리 되었습니다.");
    }

    @GetMapping("/chatings/{consultationId}")
    public ResponseEntity<?> findChating(@PathVariable("consultationId") Long consultationId) {

        List<findChatingResponse> chatingList = consultationService.findChating(consultationId);

        return ResponseEntity.status(200).body(chatingList);
    }

    @PostMapping("/chatings")
    public ResponseEntity<?> registChating(@RequestHeader(value = "Authorization", required = false) String tokenHeader,
                                           @RequestBody RegistChatingRequest registChatingRequest) {

        Long memberId = jwtUtil.extractUserId(tokenHeader.substring(7));
        String memberNickname = jwtUtil.extractNickname(tokenHeader.substring(7));

        registChatingRequest.setMemberId(memberId);
        registChatingRequest.setMemberNickname(memberNickname);

        int result = consultationService.registChating(registChatingRequest);

        if(result == 0) return ResponseEntity.status(404).body("채팅이 저장되지 않았습니다.");

        return ResponseEntity.status(200).body("정상적으로 채팅이 저장되었습니다.");
    }
}
