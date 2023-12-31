package codepred.common.util;

import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class NumberService {

    public String generateID() {
        int length = 6;
        Random random = new Random();
        StringBuilder id = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char ch = (char) (random.nextInt(26) + 'A');
            id.append(ch);
        }

        return id.toString();
    }

}
