package com.castles.seudensdocuments.core.dao;

import com.castles.seudensdocuments.core.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassportRepository extends JpaRepository<Passport, Long> {

}
