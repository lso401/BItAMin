package com.saessakmaeul.bitamin.consultations.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class JoinRoomRequest {
    private Long id;
    private LocalDateTime startTime;
    private Boolean isPrivated;
    private String password;
    private String SessionId;

    private Long memberId;
    private String memberNickname;
    private LocalDate consultationDate;
}
