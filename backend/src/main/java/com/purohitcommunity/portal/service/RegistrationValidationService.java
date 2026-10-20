package com.purohitcommunity.portal.service;
import com.purohitcommunity.portal.domain.Enums.RegistrationSource;
import com.purohitcommunity.portal.domain.RegistrationDraft;
import com.purohitcommunity.portal.web.ApiDtos.*;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class RegistrationValidationService {
    private final Validator validator;
    private final MemberIdService memberIds;
    public void personal(RegistrationSource source,String reference){
        if(source!=null && source!=RegistrationSource.DIRECT && source!=RegistrationSource.MEMBER_REFERENCE)
            throw new IllegalArgumentException("Choose Direct Registration or Member Reference");
        if(source==RegistrationSource.MEMBER_REFERENCE && (reference==null || !reference.trim().toUpperCase(java.util.Locale.ROOT).matches("(AP|TS)-[A-Z0-9]+-[0-9]{6}")))
            throw new IllegalArgumentException("Enter a valid Member Reference ID");
    }
    public String phone(String value){
        if(value==null || !value.matches("\\+?[0-9][0-9 -]*[0-9]")) throw new IllegalArgumentException("Phone number contains invalid characters");
        String digits=value.replaceAll("\\D", "");
        if(digits.length()<10 || digits.length()>15) throw new IllegalArgumentException("Phone number must contain 10 to 15 digits");
        return digits;
    }
    public void location(String state,String district){memberIds.validateLocation(state,district);}
    public void validateDraft(RegistrationDraft d){
        if(d.getAadhaarNumber()==null || !d.getAadhaarNumber().matches("[0-9]{12}")) throw new IllegalArgumentException("Enter a 12-digit Aadhaar number");
        validate(new DraftStep1Request(d.getFullName(),d.getRegistrationSource(),d.getMemberReferenceId(),d.getGender(),d.getProfession(),d.getGotram(),d.getPravara(),d.getDateOfBirth(),d.getAadhaarNumber(),d.getBloodGroup()));
        validate(new DraftStep2Request(d.getPhoneNumber(),d.getWhatsappNumber(),d.getDoorNumber(),d.getCityTown(),d.getConstituency(),d.getDistrict(),d.getPinCode(),d.getState()));
        personal(d.getRegistrationSource(),d.getMemberReferenceId());
        phone(d.getPhoneNumber());phone(d.getWhatsappNumber());location(d.getState(),d.getDistrict());
    }
    private void validate(Object request){
        var errors=validator.validate(request);
        if(!errors.isEmpty()){
            var error=errors.stream().sorted(java.util.Comparator.comparing(e->e.getPropertyPath().toString())).findFirst().orElseThrow();
            throw new IllegalArgumentException(error.getPropertyPath()+": "+error.getMessage()+". Review your saved registration details.");
        }
    }
}
