package com.purohitcommunity.portal.config;

import com.purohitcommunity.portal.domain.Enums.PlanCode;
import com.purohitcommunity.portal.domain.Enums.Role;
import com.purohitcommunity.portal.domain.MembershipPlan;
import com.purohitcommunity.portal.domain.UserAccount;
import com.purohitcommunity.portal.repository.ActivityRepository;
import com.purohitcommunity.portal.repository.MembershipPlanRepository;
import com.purohitcommunity.portal.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataInitializerTest {
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final MembershipPlanRepository plans = mock(MembershipPlanRepository.class);
    private final ActivityRepository activities = mock(ActivityRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private DataInitializer initializer;

    @BeforeEach
    void setup() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("render");
        initializer = new DataInitializer(users, plans, activities, encoder, environment);
        initializer.adminEmail = "admin@example.test";
        initializer.adminPassword = "Test-only-password-123!";
    }

    @Test
    void renderCreatesHashedAdminAndPlansWithoutSampleActivities() {
        initializer.run();
        var account = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(account.capture());
        assertEquals(Role.ADMIN, account.getValue().getRole());
        assertEquals("admin@example.test", account.getValue().getEmail());
        assertTrue(account.getValue().isEnabled());
        assertTrue(encoder.matches(initializer.adminPassword, account.getValue().getPasswordHash()));
        var plan = ArgumentCaptor.forClass(MembershipPlan.class);
        verify(plans, times(2)).save(plan.capture());
        assertEquals(PlanCode.PREMIUM, plan.getAllValues().get(0).getCode());
        assertEquals(PlanCode.ELITE, plan.getAllValues().get(1).getCode());
        verifyNoInteractions(activities);
    }

    @Test
    void renderPreservesExistingAdminAndPlans() {
        when(users.existsByEmailIgnoreCase(initializer.adminEmail)).thenReturn(true);
        when(plans.findByCode(PlanCode.PREMIUM)).thenReturn(Optional.of(new MembershipPlan()));
        when(plans.findByCode(PlanCode.ELITE)).thenReturn(Optional.of(new MembershipPlan()));
        initializer.run();
        verify(users, never()).save(any());
        verify(plans, never()).save(any());
        verifyNoInteractions(activities);
    }
}
