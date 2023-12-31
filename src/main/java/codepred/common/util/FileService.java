package codepred.common.util;

import java.io.FileOutputStream;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    @Value("${invoice_path}")
    private String invoicePath;

    public void saveFile(String fileData) {
        String filePath = invoicePath + "file.jpeg"; // Replace this with your desired file path

        try {
            // Decode the Base64 string to get the byte[]
            String[] parts = fileData.split(",");
            String base64String = parts[1]; // Assuming the Base64 content is at index 1

            // Decode the Base64 string to byte array
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);

            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(decodedBytes);
            fos.close();
        } catch (Exception e) {
        }
    }

}
