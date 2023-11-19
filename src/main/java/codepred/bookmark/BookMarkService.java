package codepred.bookmark;


import codepred.account.User;
import codepred.config.files.FileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookMarkService {

    @Autowired
    BookMarkRepository bookMarkRepository;

    public List<FileDto> getList(User tutor){
        List<BookMark> list = bookMarkRepository.findAllByUser(tutor.getId());
        return list.stream().map(b -> new FileDto(b.getFileName(),b.getDisplayedName(), b.getId())).collect(Collectors.toList());
    }

}
