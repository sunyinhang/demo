package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.SgtsLog;
import org.apache.ibatis.annotations.Param;
import org.mybatis.mapper.common.BaseMapper;

/**
 * Created by Administrator on 2017/9/3.
 */
public interface SgtsLogDao extends BaseMapper<SgtsLog>{
 Integer  selectApplSeq(@Param("applSeq") String applSeq,@Param("outSts") String outSts);
 Integer update(@Param("tscount") Integer tscount, @Param("applSeq") String applSeq,@Param("outsts") String outsts);



 Integer selectApplSeqed(@Param("applSeq")String applSeq,@Param("msgTyp")String msgTyp);
 Integer updateed(@Param("tscount")Integer tscount,@Param("applSeq")String applSeq,@Param("msgTyp")String msgTyp);

 Integer selectcount(String applSeq);
 Integer selectcounts(@Param("applSeq") String applSeq,@Param("outSts") String outSts);

 Integer selectappl(@Param("applSeq") String applSeq,@Param("outSts") String outSts);
 Integer selectapplq(@Param("applSeq") String applSeq,@Param("outSts") String outSts);

 Integer updateBySeq(@Param("applSeq") String applSeq,@Param("outSts") String outSts);

 Integer selectappltyp(@Param("applSeq") String applSeq,@Param("msgTyp") String msgTyp);

 Integer updateBytyp(@Param("applSeq") String applSeq,@Param("msgTyp") String msgTyp);


 Integer selectmsgtyp(@Param("applSeq") String applSeq,@Param("msgTyp") String msgTyp);

}
