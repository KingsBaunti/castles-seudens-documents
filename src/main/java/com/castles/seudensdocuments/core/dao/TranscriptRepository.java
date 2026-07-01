package com.castles.seudensdocuments.core.dao;

import com.castles.seudensdocuments.core.model.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
}
