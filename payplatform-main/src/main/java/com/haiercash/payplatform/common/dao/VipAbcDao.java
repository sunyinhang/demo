package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.VipAbcAppOrderGoods;
import com.haiercash.payplatform.common.data.Vipmessage;
import org.apache.ibatis.annotations.Param;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/12/25.
 */
@Repository
public interface VipAbcDao extends BaseMapper<VipAbcAppOrderGoods> {
    List<VipAbcAppOrderGoods> selectIdCard(@Param("ordersn") String ordersn, @Param("idcard") String idcard);

    //(登录接口)第一次进来的订单
    String queryvipordersnandorderno(@Param("orderSn") String orderSn);

    //订单保存后流水号，订单号的入库
    int updatevipapplseq(VipAbcAppOrderGoods vipAbcAppOrderGoods);

    //把生成的订单号根据uuid保存到vipabc_info（控制二维码失效表）表中
    int updateordersn(Vipmessage vipmessage);

    //查询第三方订单号
    String queryvipordersn(@Param("ordersn") String ordersn);

    //第三方数据进来后的保存
    void saveDataInterfaceLog(VipAbcAppOrderGoods vipAbcAppOrderGoods);

    //库里有信息就跳过，没信息就保存 VIPABC_INFO
    Integer selectuuid(@Param("uuid") String uuid);

    //VIPABC_INFO表信息的第一次保存
    void savedate(Vipmessage vipmessage);

    //没有订单号  flag  n   end time > notime
    List<Vipmessage> selectarray();

    //更新时间
    void updatetime(Vipmessage vipmessage);

    //没有产生订单号  且flag  为n 现在的时间大于end时间
    List<Vipmessage> queryvip();

    void updateflag(@Param("uuid2") String uuid2);
}
