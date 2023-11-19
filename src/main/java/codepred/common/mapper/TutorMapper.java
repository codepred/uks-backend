package codepred.common.mapper;

import codepred.account.User;
import codepred.tutor.TutorDetails;
import codepred.tutor.dto.NewTutorPersonalDataRequest;
import codepred.tutor.dto.NewTutorRequest;
import codepred.tutor.dto.UpdateTutorRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Mapper(componentModel = "spring")
public interface TutorMapper {
    User NewTutorRequestToTutor(NewTutorRequest NewTutorRequest);
    TutorDetails requestPersonalTutorDataToTutor(NewTutorPersonalDataRequest NewTutorPersonalDataRequest);


    @Mapping(source = "photo", target = "tutorDetails.photo")
    @Mapping(source = "activityType", target = "tutorDetails.activityType")
    @Mapping(source = "bankAccountNumber", target = "tutorDetails.bankAccountNumber")
    @Mapping(source = "bankName", target = "tutorDetails.bankName")
    @Mapping(source = "companyName", target = "tutorDetails.companyName")
    @Mapping(source = "name", target = "tutorDetails.name")
    @Mapping(source = "surname", target = "tutorDetails.surname")
    @Mapping(source = "nip", target = "tutorDetails.nip")
    @Mapping(source = "place", target = "tutorDetails.place")
    @Mapping(source = "postCode", target = "tutorDetails.postCode")
    @Mapping(source = "regon", target = "tutorDetails.regon")
    @Mapping(source = "street", target = "tutorDetails.street")
    @Mapping(source = "phoneNumber", target = "tutorDetails.phoneNumber")
    void updateFromDto(UpdateTutorRequest dto, @MappingTarget User entity);

     default byte[] cropToCircle(MultipartFile originalFile) throws IOException {
        BufferedImage originalImage;

        if (originalFile == null || originalFile.isEmpty()) {
            return null;
        } else {
            originalImage = ImageIO.read(originalFile.getInputStream());
        }

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(circleImage, "png", baos);
        baos.flush();
        byte[] croppedImageBytes = baos.toByteArray();
        baos.close();

        return croppedImageBytes;
    }
}
