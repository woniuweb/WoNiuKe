package top.naccl.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UploadFileVO {
	private Long id;
	private String url;
	private String fileName;
	private String originalName;
	private String fileType;
	private Long fileSize;
	private String storageMode;
	private String filePath;
}
