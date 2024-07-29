package com.saessakmaeul.bitamin.consultations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RegistRoomResponse {
    private Long id;
    private Boolean isPrivated;
    private String password;
}
