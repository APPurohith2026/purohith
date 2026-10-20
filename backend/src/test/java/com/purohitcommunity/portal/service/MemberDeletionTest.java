package com.purohitcommunity.portal.service;

import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.Role;
import com.purohitcommunity.portal.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberDeletionTest {
    @Mock UserAccountRepository users;
    @Mock MemberProfileRepository members;
    @Mock MembershipRepository memberships;
    @Mock ActivityRegistrationRepository registrations;
    @Mock DigitalIdCardRepository cards;
    @Mock RegistrationDraftRepository drafts;
    @InjectMocks PortalService portal;

    @Test void deletesAccountAndDependenciesWhilePreservingReferredMembers(){
        var user=UserAccount.builder().role(Role.MEMBER).phoneNumber("9000000001").build();
        var member=MemberProfile.builder().id(1L).user(user).build();
        var referred=MemberProfile.builder().id(2L).referrerMember(member).memberReferenceId("REF").build();
        var card=new DigitalIdCard();
        var registration=new ActivityRegistration();
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(members.findByReferrerMemberId(1L)).thenReturn(List.of(referred));
        when(cards.findByMemberId(1L)).thenReturn(Optional.of(card));
        when(registrations.findByMemberIdOrderByRegisteredAtDesc(1L)).thenReturn(List.of(registration));
        portal.deleteMember(1L);
        assertNull(referred.getReferrerMember());
        assertNull(referred.getMemberReferenceId());
        verify(registrations).deleteAll(List.of(registration));
        verify(cards).delete(card);
        verify(memberships).deleteByMemberId(1L);
        verify(drafts).deleteByPhoneNumber("9000000001");
        var order=inOrder(members,users);
        order.verify(members).delete(member);
        order.verify(members).flush();
        order.verify(users).delete(user);
        verify(members,never()).delete(referred);
    }

    @Test void refusesToDeleteAdminAccounts(){
        var member=MemberProfile.builder().user(UserAccount.builder().role(Role.ADMIN).build()).build();
        when(members.findById(1L)).thenReturn(Optional.of(member));
        assertThrows(IllegalArgumentException.class,()->portal.deleteMember(1L));
        verifyNoInteractions(users,memberships,registrations,cards,drafts);
    }

    @Test void missingMemberDoesNotDeleteAnything(){
        when(members.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,()->portal.deleteMember(1L));
        verifyNoInteractions(users,memberships,registrations,cards,drafts);
    }
}
