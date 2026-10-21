package com.purohitcommunity.portal.service;

import com.purohitcommunity.portal.domain.Enums.RegistrationSource;
import com.purohitcommunity.portal.web.ApiDtos.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidationServiceTest {
    final Validator validator=Validation.buildDefaultValidatorFactory().getValidator();
    final RegistrationValidationService rules=new RegistrationValidationService(validator,new MemberIdService());
    DraftStep1Request personal(LocalDate date,String name){return new DraftStep1Request(name,RegistrationSource.DIRECT,null,"MALE","Purohit","Bharadwaja",null,date,"123456789012","O+",null,null);}

    @Test void datesRequireFourDigitYearAndCannotBeInTheFuture(){
        for(LocalDate date:new LocalDate[]{LocalDate.of(999,1,1),LocalDate.of(10000,1,1),LocalDate.now().plusDays(1)})
            assertFalse(validator.validate(personal(date,"Test Member")).isEmpty());
        assertTrue(validator.validate(personal(LocalDate.of(2000,2,29),"Test Member")).isEmpty());
        assertTrue(validator.validate(personal(LocalDate.now(),"Test Member")).isEmpty());
        assertFalse(validator.validate(personal(null,"Test Member")).isEmpty());
    }
    @Test void whitespaceNamesAndOversizedValuesAreRejected(){
        assertFalse(validator.validate(personal(LocalDate.of(1990,1,1),"   ")).isEmpty());
        assertFalse(validator.validate(personal(LocalDate.of(1990,1,1),"a".repeat(141))).isEmpty());
    }
    @Test void optionalFinalPasswordCannotBeBlankShortOrExceedBcryptByteLimit(){
        for(String password:new String[]{"        ","short","a".repeat(73),"అ".repeat(25)})
            assertFalse(validator.validate(new DraftConfirmRequest(password,true)).isEmpty());
        assertTrue(validator.validate(new DraftConfirmRequest(null,true)).isEmpty());
        assertTrue(validator.validate(new DraftConfirmRequest("GoodPassword123!",true)).isEmpty());
        assertFalse(validator.validate(new DraftConfirmRequest("GoodPassword123!",false)).isEmpty());
    }
    @Test void phoneValidationChecksFormatAndDigitCount(){
        for(String phone:new String[]{"12345","1234567890123456","++919000000001","900000000a","          "})
            assertThrows(IllegalArgumentException.class,()->rules.phone(phone));
        assertEquals("919000000001",rules.phone("+91 90000-00001"));
    }
    @Test void validatesStateDistrictPairAndReferenceSource(){
        assertDoesNotThrow(()->rules.location("Andhra Pradesh","East Godavari"));
        assertDoesNotThrow(()->rules.location("Telangana","Hyderabad"));
        assertThrows(IllegalArgumentException.class,()->rules.location("Andhra Pradesh","Hyderabad"));
        assertThrows(IllegalArgumentException.class,()->rules.location("Invalid","East Godavari"));
        assertThrows(IllegalArgumentException.class,()->rules.personal(RegistrationSource.ADMIN_CREATED,null));
        assertThrows(IllegalArgumentException.class,()->rules.personal(RegistrationSource.MEMBER_REFERENCE,"invalid"));
        assertDoesNotThrow(()->rules.personal(RegistrationSource.MEMBER_REFERENCE,"AP-EGD-000001"));
    }
    @Test void rejectsInvalidPinAndUnsupportedSelections(){
        var request=new DraftStep2Request("9000000001","9000000001","1","Town","City","East Godavari","000000","Andhra Pradesh",null);
        assertTrue(validator.validate(request).stream().anyMatch(e->e.getPropertyPath().toString().equals("pinCode")));
        var invalid=new DraftStep1Request("Name",RegistrationSource.DIRECT,null,"INVALID","Purohit","Gotram",null,LocalDate.of(1990,1,1),"123456789012","INVALID",null,null);
        assertEquals(2,validator.validate(invalid).size());
    }
    @Test void gothramAcceptsLettersAndRejectsNumbersAndSymbols(){
        for(String gotram:new String[]{"Bharadwaja","Kashyapa Gothram","భరద్వాజ"}){
            var request=new DraftStep1Request("Member",RegistrationSource.DIRECT,null,"MALE","Purohit",gotram,null,LocalDate.of(1990,1,1),"123456789012","O+",null,null);
            assertTrue(validator.validate(request).isEmpty(),gotram);
        }
        for(String gotram:new String[]{"123","Bharadwaja1","Kashyapa@","౧౨౩"}){
            var request=new DraftStep1Request("Member",RegistrationSource.DIRECT,null,"MALE","Purohit",gotram,null,LocalDate.of(1990,1,1),"123456789012","O+",null,null);
            assertFalse(validator.validate(request).isEmpty(),gotram);
        }
    }
}
