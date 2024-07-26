package com.saessakmaeul.bitamin.consultations.service;

import com.saessakmaeul.bitamin.consultations.repository.ConsultationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulingService {
    private final ConsultationRepository consultationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldConsultations() {
        List<Long> id = consultationRepository.findIdsOfOldConsultations();

        consultationRepository.deleteAllById(id);
    }
}