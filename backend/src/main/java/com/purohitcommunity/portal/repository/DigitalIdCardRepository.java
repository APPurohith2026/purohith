package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.DigitalIdCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DigitalIdCardRepository extends JpaRepository<DigitalIdCard,Long>{ Optional<DigitalIdCard> findByMemberId(Long memberId); Optional<DigitalIdCard> findByQrTokenAndActiveTrue(String qrToken); }
