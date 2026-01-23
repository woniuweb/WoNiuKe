package top.naccl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UploadFile {
	private Long id;
	private String storageMode;
	private String fileType;
	private String originalName;
	private String fileName;
	private String filePath;
	private String url;
	private Long fileSize;
	private Date createTime;
}
