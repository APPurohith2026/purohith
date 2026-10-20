package com.purohitcommunity.portal.web;

import com.purohitcommunity.portal.config.SecurityConfig;
import com.purohitcommunity.portal.security.*;
import com.purohitcommunity.portal.service.PortalService;
import com.purohitcommunity.portal.storage.PrivateFileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class,JwtAuthFilter.class})
class AdminDocumentAccessTest {
    @Autowired MockMvc mvc;
    @MockitoBean PortalService portal;
    @MockitoBean PrivateFileStorageService storage;
    @MockitoBean JwtService jwt;
    @MockitoBean CustomUserDetailsService userDetails;

    @Test void anonymousAndMembersCannotReadIdentityDocuments() throws Exception {
        for(String suffix:new String[]{"documents/aadhaar-front","documents/aadhaar-back","passport-photo"}){
            mvc.perform(get("/api/admin/members/1/"+suffix)).andExpect(status().isForbidden());
            mvc.perform(get("/api/admin/members/1/"+suffix).with(user("member").roles("MEMBER"))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(portal,storage);
    }

    @Test void adminReadsOnlyTheRequestedMembersDocument() throws Exception {
        var member=mock(ApiDtos.MemberView.class);
        when(portal.member(1L)).thenReturn(member);
        when(member.aadhaarFrontPath()).thenReturn("private/front.pdf");
        when(storage.document("private/front.pdf")).thenReturn(ResponseEntity.ok().body(new byte[]{1,2,3}));
        mvc.perform(get("/api/admin/members/1/documents/aadhaar-front").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().bytes(new byte[]{1,2,3}));
        verify(storage).document("private/front.pdf");
    }

    @Test void unsupportedDocumentTypesAreNotFound() throws Exception {
        when(portal.member(1L)).thenReturn(mock(ApiDtos.MemberView.class));
        mvc.perform(get("/api/admin/members/1/documents/arbitrary-file").with(user("admin").roles("ADMIN")))
            .andExpect(status().isNotFound());
        verifyNoInteractions(storage);
    }
}
