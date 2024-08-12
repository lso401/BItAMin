package com.saessakmaeul.bitamin.mission.repository;

import com.saessakmaeul.bitamin.mission.entity.MemberPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPhraseRepository extends JpaRepository<MemberPhrase, Long> {
}