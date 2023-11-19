package codepred.common.mapper;

import codepred.company.Company;
import codepred.company.dto.CompanyGroupCreateRequest;
import codepred.company.dto.CreateCompanyRequest;
import codepred.company.dto.UpdateCompanyRequest;
import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    List<CompanyGroupCreateRequest> toCreateCompanyGroupDTO(List<Company> companyList);

    Company fromRequestDTO(CreateCompanyRequest createCompanyRequest);

    Company fromRequestCompanyToUpdateDTO(UpdateCompanyRequest updateCompanyRequest);

    default byte[] cropToCircle(MultipartFile originalFile) throws IOException {
        BufferedImage originalImage;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (originalFile == null || originalFile.isEmpty()) {
            return null;
        } else {
            originalImage = ImageIO.read(originalFile.getInputStream());

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int size = Math.min(width, height);
            int x = (width - size) / 2;
            int y = (height - size) / 2;

            int targetSize = 150;
            BufferedImage circleImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = circleImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setClip(new Ellipse2D.Float(0, 0, targetSize, targetSize));
            g2d.drawImage(originalImage, 0, 0, targetSize, targetSize, x, y, x + size, y + size, null);
            g2d.dispose();

            ImageIO.write(circleImage, "png", baos);
        }

        baos.flush();
        byte[] imageBytes = baos.toByteArray();
        baos.close();
        return imageBytes;
    }
}
