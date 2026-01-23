package top.naccl.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.naccl.entity.UploadFile;

@Mapper
@Repository
public interface UploadFileMapper {
	int save(UploadFile file);

	UploadFile getById(Long id);
}
