package com.purohitcommunity.portal.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MemberIdService {
    private static final Map<String,String> STATE_CODES = Map.of(
        "Andhra Pradesh", "AP",
        "Telangana", "TS"
    );

    private static final Map<String,String> DISTRICT_CODES = createDistrictCodes();

    public String stateCode(String state) {
        String code = STATE_CODES.get(state);
        if (code == null) throw new IllegalArgumentException("Unsupported state: " + state);
        return code;
    }

    public String districtCode(String district) {
        String code = DISTRICT_CODES.get(normalize(district));
        if (code == null) throw new IllegalArgumentException("Unsupported district: " + district);
        return code;
    }

    public String generate(String state, String district, long generatedId) {
        return stateCode(state) + "-" + districtCode(district) + "-" + String.format("%06d", generatedId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('–','-');
    }

    public void validateLocation(String state,String district){
        String selectedState=stateCode(state);
        String code=districtCode(district);
        Set<String> telangana=Set.of("ADB","BKG","HNK","HYD","JGL","JGN","JSB","JOG","KMR","KRM","KHM","KBA","MBD","MBN","MCL","MDK","MCM","MLG","NGK","NLG","NRP","NRM","NZB","PDL","RJS","RRD","SGR","SDP","SRP","VKB","WNP","WRG","YBG");
        if(!selectedState.equals(telangana.contains(code)?"TS":"AP")) throw new IllegalArgumentException("District does not belong to the selected state");
    }

    private static Map<String,String> createDistrictCodes() {
        Map<String,String> m = new HashMap<>();
        put(m,"Alluri Sitharama Raju","ASR"); put(m,"Anakapalli","AKP"); put(m,"Ananthapuramu","ATP"); put(m,"Annamayya","ANN");
        put(m,"Bapatla","BAP"); put(m,"Chittoor","CTR"); put(m,"Dr. B. R. Ambedkar Konaseema","KON"); put(m,"East Godavari","EGD");
        put(m,"Eluru","ELR"); put(m,"Guntur","GNT"); put(m,"Kakinada","KAK"); put(m,"Krishna","KRI"); put(m,"Kurnool","KUR");
        put(m,"Nandyal","NDY"); put(m,"NTR","NTR"); put(m,"Palnadu","PLN"); put(m,"Parvathipuram Manyam","PVM"); put(m,"Prakasam","PKM");
        put(m,"Sri Potti Sriramulu Nellore","NLR"); put(m,"Sri Sathya Sai","SSS"); put(m,"Srikakulam","SKL"); put(m,"Tirupati","TPT");
        put(m,"Visakhapatnam","VSP"); put(m,"Vizianagaram","VZM"); put(m,"West Godavari","WGD"); put(m,"YSR Kadapa","KDP");

        put(m,"Adilabad","ADB"); put(m,"Bhadradri Kothagudem","BKG"); put(m,"Hanumakonda","HNK"); put(m,"Hyderabad","HYD");
        put(m,"Jagtial","JGL"); put(m,"Jangaon","JGN"); put(m,"Jayashankar Bhupalpally","JSB"); put(m,"Jogulamba Gadwal","JOG");
        put(m,"Kamareddy","KMR"); put(m,"Karimnagar","KRM"); put(m,"Khammam","KHM"); put(m,"Komaram Bheem Asifabad","KBA");
        put(m,"Mahabubabad","MBD"); put(m,"Mahabubnagar","MBN"); put(m,"Mancherial","MCL"); put(m,"Medak","MDK");
        put(m,"Medchal-Malkajgiri","MCM"); put(m,"Medchal–Malkajgiri","MCM"); put(m,"Mulugu","MLG"); put(m,"Nagarkurnool","NGK");
        put(m,"Nalgonda","NLG"); put(m,"Narayanpet","NRP"); put(m,"Nirmal","NRM"); put(m,"Nizamabad","NZB"); put(m,"Peddapalli","PDL");
        put(m,"Rajanna Sircilla","RJS"); put(m,"Rangareddy","RRD"); put(m,"Sangareddy","SGR"); put(m,"Siddipet","SDP");
        put(m,"Suryapet","SRP"); put(m,"Vikarabad","VKB"); put(m,"Wanaparthy","WNP"); put(m,"Warangal","WRG"); put(m,"Yadadri Bhuvanagiri","YBG");
        return Collections.unmodifiableMap(m);
    }

    private static void put(Map<String,String> m, String district, String code) { m.put(normalize(district), code); }
}
