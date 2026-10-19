package com.purohitcommunity.portal.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemberIdServiceTest {
  private final MemberIdService service = new MemberIdService();

  @Test void generatesTelanganaHyderabadId(){
    assertEquals("TS-HYD-000125", service.generate("Telangana", "Hyderabad", 125));
  }

  @Test void generatesAndhraGunturId(){
    assertEquals("AP-GNT-000007", service.generate("Andhra Pradesh", "Guntur", 7));
  }

  @Test void rejectsUnknownDistrict(){
    assertThrows(IllegalArgumentException.class, () -> service.generate("Telangana", "Unknown", 1));
  }

  @Test void handlesMedchalDashVariants(){
    assertEquals("MCM", service.districtCode("Medchal-Malkajgiri"));
    assertEquals("MCM", service.districtCode("Medchal–Malkajgiri"));
  }
}
