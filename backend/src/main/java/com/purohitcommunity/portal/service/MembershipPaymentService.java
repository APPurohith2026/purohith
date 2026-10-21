package com.purohitcommunity.portal.service;

import com.purohitcommunity.portal.domain.Membership;
import com.purohitcommunity.portal.domain.Enums.PaymentStatus;
import com.purohitcommunity.portal.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

@Service @RequiredArgsConstructor
public class MembershipPaymentService {
    private final MembershipRepository memberships;
    public record PaymentView(String trackingId, String reference, PaymentStatus status,
        BigDecimal amount, String planCode, Instant submittedAt, Instant verifiedAt) {}
    @Transactional(readOnly=true) public PaymentView get(Long memberId){ return view(find(memberId)); }
    private Membership find(Long memberId){ return memberships.findFirstByMemberIdOrderByIdDesc(memberId).orElseThrow(); }
    private PaymentView view(Membership m){ return new PaymentView(m.getPaymentTrackingId(),m.getPaymentReference(),m.getPaymentStatus(),m.getPaymentAmount(),m.getPlan().getCode().name(),m.getPaymentSubmittedAt(),m.getPaymentVerifiedAt()); }
    @Transactional public PaymentView submit(Long memberId,String reference){
        var m=memberships.findTopByMemberIdOrderByIdDesc(memberId).orElseThrow();
        if(m.getPaymentTrackingId()==null) throw new IllegalStateException("No payment request exists for this membership");
        if(m.getPaymentStatus()==PaymentStatus.VERIFIED) throw new IllegalStateException("Payment has already been verified");
        String normalized=reference==null?"":reference.trim().toUpperCase(Locale.ROOT);
        if(!normalized.matches("[A-Z0-9][A-Z0-9-]{5,63}")) throw new IllegalArgumentException("Enter the transaction reference from your UPI app (6–64 letters or digits)");
        if(memberships.existsByPaymentReferenceAndIdNot(normalized,m.getId())) throw new IllegalArgumentException("This transaction reference has already been submitted");
        m.setPaymentReference(normalized); m.setPaymentStatus(PaymentStatus.MANUAL_REVIEW);
        m.setPaymentSubmittedAt(Instant.now()); m.setPaymentVerifiedAt(null);
        memberships.saveAndFlush(m); return view(m);
    }
    @Transactional public PaymentView review(Long memberId,boolean received,String expectedReference){
        var m=memberships.findTopByMemberIdOrderByIdDesc(memberId).orElseThrow();
        if(!java.util.Objects.equals(expectedReference,m.getPaymentReference())) throw new IllegalStateException("Payment reference changed. Refresh and check the current transaction before verifying");
        if(m.getPaymentStatus()!=PaymentStatus.MANUAL_REVIEW || m.getPaymentReference()==null) throw new IllegalStateException("Only a submitted payment can be reviewed");
        m.setPaymentStatus(received?PaymentStatus.VERIFIED:PaymentStatus.FAILED);
        m.setPaymentVerifiedAt(received?Instant.now():null);
        memberships.saveAndFlush(m); return view(m);
    }
}
