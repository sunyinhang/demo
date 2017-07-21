package com.haiercash.common.data;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "APP_FILE_VERSION")

public class AppFile {
	@Id
	private @GeneratedValue(strategy = GenerationType.SEQUENCE) Long id;
	private String fileName;// 文件名
	private String updateDate;// 更新日期
	private String version;// 版本号
	// 将版本号拆分为大版本、中版本、小版本
	private Integer versionBig;
	private Integer versionMiddle;
	private Integer versionSmall;
	private String sysVersion;// 系统的版本
	private String storePath;// 存储路径
	private String isForceUpdate;// 是否强制更新
	private Long downloadCount;// 下载次数
	private Long fileSize;// 版本号
	// private MultipartFile myfile;//上传的文件

	public Integer getVersionBig() {
		return versionBig;
	}

	public void setVersionBig(Integer versionBig) {
		this.versionBig = versionBig;
	}

	public Integer getVersionMiddle() {
		return versionMiddle;
	}

	public void setVersionMiddle(Integer versionMiddle) {
		this.versionMiddle = versionMiddle;
	}

	public Integer getVersionSmall() {
		return versionSmall;
	}

	public void setVersionSmall(Integer versionSmall) {
		this.versionSmall = versionSmall;
	}

	public String getSysVersion() {
		return sysVersion;
	}

	public void setSysVersion(String sysVersion) {
		this.sysVersion = sysVersion;
	}

	public String getStorePath() {
		return storePath;
	}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public String getIsForceUpdate() {
		return isForceUpdate;
	}

	public void setIsForceUpdate(String isForceUpdate) {
		this.isForceUpdate = isForceUpdate;
	}

	public Long getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(Long downloadCount) {
		this.downloadCount = downloadCount;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		if (updateDate == null || "".equals(updateDate)) {
			this.updateDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

		} else {
			this.updateDate = updateDate;
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getFileSize() {
		return fileSize;
	}

	// public MultipartFile getMyfile() {
	// return myfile;
	// }
	// public void setMyfile(MultipartFile myfile) {
	// this.myfile = myfile;
	// }
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
