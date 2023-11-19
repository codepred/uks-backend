package codepred.whitelist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhitelistService {

    @Value("${is_whitelist_activated}")
    private Boolean isWhitelistActivated;

    @Autowired
    private WhitelistRepo whitelistRepo;

    public boolean canAddTutor(String email){
        if(isWhitelistActivated){
            if(whitelistRepo.getWhitelistByEmail(email) != null){
                return true;
            }
            return false;
        }
        return true;
    }

}