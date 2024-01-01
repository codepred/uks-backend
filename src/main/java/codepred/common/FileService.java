package codepred.common;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    @Value("${invoice_path}")
    private String invoicePath;

    public void saveFile(String fileData) {
        final var filePath = invoicePath + "file.jpeg";

        try {
            final var parts = fileData.split(",");
            final var base64String = parts[1];
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);

            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(decodedBytes);
            fos.close();
        } catch (Exception e) {
        }
    }

    public void deleteFile(String filePath) {
        final var fileToDelete = new File(filePath);

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                System.out.println("File deleted successfully: " + filePath);
            } else {
                System.out.println("Failed to delete the file: " + filePath);
            }
        } else {
            System.out.println("File does not exist: " + filePath);
        }
    }

}
