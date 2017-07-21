package com.haiercash.appserver.service.impl;

import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CmisService;
import com.haiercash.appserver.web.CmisController;
import com.haiercash.common.data.AttachFile;
import com.haiercash.common.data.AttachFileRepository;
import com.haiercash.common.data.FTPBean;
import com.haiercash.common.data.FTPBeanListInfo;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.haiercash.appserver.util.sign.FileSignUtil.getBusinessFlag;
import static com.haiercash.appserver.util.sign.FileSignUtil.getSystemFlag;
import static com.haiercash.commons.util.RestUtil.fail;
import static com.haiercash.commons.util.RestUtil.success;

/**
 * Cmis Service impl.
 *
 * @author Liu qingxiang
 * @since v1.5.2
 */
@Service
public class CmisServiceImpl implements CmisService {
    private Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private AttachFileRepository attachFileRepository;
    @Autowired
    private AttachService attachService;

    public Map<String, Object> ftpBean(FTPBean ftpBean, AttachFile attachFile) {
        try {
            Map<String, Object> cmisResult = new CmisController().addFTPInterface(ftpBean);
            logger.info("添加影像返回：" + cmisResult);
            if (cmisResult == null || !CmisUtil.getIsSucceed(cmisResult)) {
                //如果信贷返回失败，则把原来存的数据库数据删除，文件删除
                attachFileRepository.delete(attachFile);//删除数据
                //删除文件
                File file = new File(attachFile.getFileName());
                if (file.exists()) {
                    file.delete();
                }
                //核心影像至失效
                List<FTPBeanListInfo> fileList = ftpBean.getList();
                for (FTPBeanListInfo fileInfo: fileList){
                    fileInfo.setState("0");// 1:有效 0:失效
                }
                Map<String, Object> cmisUpdateResult = new CmisController().updateFTPInterface(ftpBean);
                logger.info("删除影像返回：" + cmisUpdateResult);

                return fail("90", CmisUtil.getErrMsg(cmisResult));
            } else {
                // 保存信贷系统生成的文件唯一编号
                Map<String, Object> maplist = CmisUtil.getDataMap(cmisResult, "list");
                Object objInfo = maplist.get("info");
                String attachSeq;
                if (objInfo instanceof Map) {
                    Map<String, Object> mapInfo = (Map) objInfo;
                    attachSeq = mapInfo.get("attachSeq").toString();
                } else if (objInfo instanceof List) {
                    List<Map<String, Object>> listInfo = (List) objInfo;
                    attachSeq = listInfo.get(0).get("attachSeq").toString();
                } else {
                    attachFileRepository.delete(attachFile);
                    return fail("13", "保存文件失败: 无法获取文件唯一编号");
                }
                attachFile.setAttachSeq(attachSeq);
                attachFileRepository.save(attachFile);
            }
        } catch (Exception e) {
            return fail("12", "保存文件失败");
        }
        return success();
    }

    @Override
    public Map<String, Object> ftpBeanDoc5354(FTPBean ftpBean, AttachFile attachFile, String idNo) {
        List<AttachFile> attachFileList = attachFileRepository
                .findByCustNoAndAttachType(attachFile.getCustNo(), attachFile.getAttachType());
        Map<String, Object> result = this.ftpBean(ftpBean, attachFile);
        Object head = result.get("head");
        if (head instanceof ResultHead) {
            ResultHead resultHead = (ResultHead) head;
            String retFlag = resultHead.getRetFlag();
            if (!"00000".equals(retFlag)) {
                return fail("21", "影像上传核心失败");
            }
        }
        for (AttachFile file : attachFileList) {
            logger.info(file.getFileName() + "删除原照片");
            if (!file.getFileName().equals(attachFile.getFileName())) {
                ftpBean.setSysId(getSystemFlag());
                ftpBean.setBusId(getBusinessFlag());
                ftpBean.setApplSeq(idNo);
                FTPBeanListInfo fileInfo = new FTPBeanListInfo();
                fileInfo.setSequenceId(attachFile.getId().toString());
                fileInfo.setAttachPath(file.getFileName());// 完整路径
                //fileInfo.setAttachName(originalFilename);//不再用原文件名，改用uuid组成的文件名
                String fileName = attachService.getFileName(file.getFileName());
                fileInfo.setAttachName(fileName);
                fileInfo.setAttachNameNew(fileName);
                fileInfo.setState("0");// 1:有效 0:无效
                fileInfo.setCrtUsr("admin");
                fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                fileInfo.setAttachTyp(attachFile.getAttachType());
                List<FTPBeanListInfo> fileList = new ArrayList<>();
                fileList.add(fileInfo);
                ftpBean.setList(fileList);
                Map<String, Object> map = this.ftpBean(ftpBean, file);
                logger.info("ftp上传后返回map:" + map);
                head = map.get("head");
                if (head instanceof ResultHead) {
                    ResultHead resultHead = (ResultHead) head;
                    String retFlag = resultHead.getRetFlag();
                    if (!"00000".equals(retFlag)) {
                        return fail("21", "影像上传核心失败");
                    }
                    // 删除原身份证照片
                    attachFileRepository.delete(file);
                    //删除文件
                    File oFile = new File(file.getFileName());
                    if (oFile.exists()) {
                        oFile.delete();
                    }

                } else {
                    if (!HttpUtil.isSuccess(map)) {
                        return fail("21", "影像上传核心失败");
                    }
                }

            }
        }
        return success();
    }

}
