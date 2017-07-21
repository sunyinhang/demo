package com.haiercash.common.data;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class AppOrderRepositoryImpl {
    @PersistenceContext
    EntityManager entityManager;

    /**
     * 查询订单明细，支持分页；分页参数小于0时，查询全部
     *
     * @param crtUsr
     * @param status 订单状态：1-待提交，2-待确认，3-被退回
     * @param page
     * @param size
     * @return
     */
    public List queryAppOrder(String crtUsr, String status, Integer page, Integer size, String custNo, String type, String source) {
        int start = -1;
        int end = -1;
        if (page != null && size != null && page > 0 && size > 0) {
            start = (page - 1) * size + 1;
            end = start + size - 1;
        }
        String sql;
        String sourceCondition;
        if ("cust".equals(type)) {
            if (StringUtils.isEmpty(source) || "14".equals(source)) {//嗨付
                //嗨付可以查看集团大数据和美分期
                sourceCondition = " and (source='2' OR  source = '3' OR (source='11' and  channel_no = '34') OR (source='11' and channel_no = '35')) ";
            } else {//星巢贷、美分期等
                if ("34".equals(source) || "35".equals(source)) {
                    //集团大数据与嗨付、美分期与嗨付 互看
                    sourceCondition = " and (channel_no ='" + source + "' OR source='2' OR source = '3') ";
                } else {
                    //只查自己渠道
                    if ("16".equals(source)){//星巢贷
                        sourceCondition = " and (source ='" + source + "')";
                    }else {
                        sourceCondition = " and (channel_no ='" + source + "')";
                    }
                }
            }
            sql = "select order_no, apply_Amt, apply_Tnr,typ_Grp,apply_Dt, " + "      (select goods_name "
                    + "    	       from app_order_goods  " + "    	       where order_no = app_order.order_no  "
                    + "    	         and rownum = 1) goods_name,  " + "    	         (SELECT COUNT(1)  "
                    + "    	                FROM app_order_goods "
                    + "    	               WHERE order_no = app_order.order_no) goods_count, "
                    + "    	                     nvl(totalnormint, 0) + nvl(totalfeeamt, 0) as fee, "
                    + "    	                     APPLY_TNR_TYP, " + "    	                     rownum rowno, "
                    + "  coopr_cde,coopr_Name,cust_name   	 from   app_order " + "    	 where  cust_no='" + custNo
                    + "'  and status = '" + status + "' " + sourceCondition
                    + "   	   and (typ_grp <> '01' or pro_pur_amt > 0)  " + "   	   order by create_time desc  ";
        } else {
            // 数据量不大且结果集小，这里不考虑效率问题，直接用子查询查第一个商品
            sql = "select order_no,apply_Amt,apply_Tnr,typ_Grp,apply_Dt,"
                    + "(select goods_name from app_order_goods where order_no = app_order.order_no and rownum = 1) goods_name,"
                    + "(SELECT COUNT(1) FROM app_order_goods WHERE order_no = app_order.order_no) goods_count,"
                    + "nvl(totalnormint,0)+nvl(totalfeeamt,0) as fee,APPLY_TNR_TYP,"
                    + "rownum rowno ,coopr_cde,coopr_name,cust_name   from app_order where crt_usr='" + crtUsr
                    + "' and status = '" + status + "' and (typ_grp <> '01' or pro_pur_amt > 0)"
                    + "  order by create_time desc";
        }

        if (start >= 0) {
            sql = String.format("select * from ( select t.*, rownum rn from (%s) t where rownum <= %d) where rn >= %d",
                    sql, end, start);
        }

        Query query = entityManager.createNativeQuery(sql);

        return query.getResultList();
    }

    /**
     * 查询订单明细，支持分页；分页参数小于0时，查询全部
     *
     * @param crtUsr
     * @param status
     * @param page
     * @param size
     * @return
     */
    public List queryAppOrderByIdNo(String crtUsr, String status, Integer page, Integer size, String idNo,
                                    String type, String source) {
        int start = -1;
        int end = -1;
        if (page != null && size != null && page > 0 && size > 0) {
            start = (page - 1) * size + 1;
            end = start + size - 1;
        }
        String sql;
        String sourceCondition;
        if ("cust".equals(type)) {
            if (StringUtils.isEmpty(source) || "14".equals(source)) {//嗨付
                //嗨付可以查看集团大数据和美分期
                sourceCondition = " and (source='2' OR  source = '3' OR (source='11' and  channel_no = '34') OR (source='11' and channel_no = '35')) ";
            } else {//星巢贷、美分期等
                if ("34".equals(source) || "35".equals(source)) {
                    //集团大数据与嗨付、美分期与嗨付 互看
                    sourceCondition = " and (channel_no ='" + source + "' OR source='2' OR source = '3') ";
                } else {
                    //只查自己渠道
                    if ("16".equals(source)){//星巢贷
                        sourceCondition = " and (source ='" + source + "')";
                    }else {
                        sourceCondition = " and (channel_no ='" + source + "')";
                    }
                }
            }

            sql = "select apply_Amt, apply_Tnr,typ_Grp,apply_Dt, applseq,typ_Level_Two,nvl(month_repay,0)+0 mth_Amt,mtd_name,mtd_Cde,crt_usr, apprv_amt,"
                    + "      (select goods_name " + "    	       from app_order_goods  "
                    + "    	       where order_no = app_order.order_no  "
                    + "    	         and rownum = 1) goods_name,  " + "    	         (SELECT COUNT(1)  "
                    + "    	                FROM app_order_goods "
                    + "    	               WHERE order_no = app_order.order_no) goods_count, "
                    + "    	                     nvl(totalnormint, 0) + nvl(totalfeeamt, 0) as fee, "
                    + "    	                     APPLY_TNR_TYP, "
                    + "    	                     rownum rowno ,order_no,status,cust_name" + "    	 from   app_order "
                    + "    	 where id_no='" + idNo + "'  and status in(" + status + ") " + sourceCondition
                    + "   	   and (typ_grp <> '01' or pro_pur_amt > 0)  " + "   	   order by create_time desc  ";

        } else {
            // 数据量不大且结果集小，这里不考虑效率问题，直接用子查询查第一个商品
            sql = "select apply_Amt,apply_Tnr,typ_Grp,apply_Dt,applseq,typ_Level_Two,nvl(month_repay,0)+0 mth_Amt,mtd_name,mtd_Cde,crt_usr,apprv_amt,"
                    + "(select goods_name from app_order_goods where order_no = app_order.order_no and rownum = 1) goods_name,"
                    + "(SELECT COUNT(1) FROM app_order_goods WHERE order_no = app_order.order_no) goods_count,"
                    + "nvl(totalnormint,0)+nvl(totalfeeamt,0) as fee,APPLY_TNR_TYP,"
                    + "rownum rowno,order_no,status,cust_name   from app_order where crt_usr='" + crtUsr
                    + "' and status in(" + status + ") and (typ_grp <> '01' or pro_pur_amt > 0)"
                    + "  order by create_time desc";
        }

        if (start >= 0) {
            sql = String.format("select * from ( select t.*, rownum rn from (%s) t where rownum <= %d) where rn >= %d",
                    sql, end, start);
        }

        Query query = entityManager.createNativeQuery(sql);

        return query.getResultList();
    }

    /**
     * 查询订单数量
     *
     * @param crtUsr
     * @param status 订单状态：1-待提交，2-待确认，3-被退回
     * @return
     */
    public int getAppOrderCount(String crtUsr, String status) {
        String sql = "select count(1) from app_order where crt_usr=?1 and status = ?2 and (typ_grp <> '01' or pro_pur_amt > 0)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, crtUsr);
        query.setParameter(2, status);
        return Integer.parseInt(query.getSingleResult().toString());
    }

    /**
     * 查询订单数量个人版
     *
     * @param custNo
     * @param status
     * @return
     */
    public int getAppOrderCountCust(String custNo, String status, String sourceStr) {
        String sql;
        if (StringUtils.isEmpty(sourceStr) || "14".equals(sourceStr)) {//嗨付
            // sql = "select count(1) from app_order where  cust_No=?1 and (source='2' OR  source = '3') and status = ?2 and (typ_grp <> '01' or pro_pur_amt > 0)";
            //嗨付可以查看集团大数据和美分期
            sql = "select count(1) from app_order where  cust_No=?1 and (source='2' OR  source = '3' OR (source='11' and  channel_no = '34') OR (source='11' and channel_no = '35')) and status = ?2 and (typ_grp <> '01' or pro_pur_amt > 0)";
        } else {//星巢贷、美分期等
            if ("34".equals(sourceStr) || "35".equals(sourceStr)) {
                //集团大数据与嗨付、美分期与嗨付 互看
                sql = "select count(1) from app_order where  cust_No=?1 and (channel_no ='" + sourceStr + "' OR source='2' OR  source = '3' ) and status = ?2 and (typ_grp <> '01' or pro_pur_amt > 0)";
            } else {//星巢贷
                //只查自己渠道
                if ("16".equals(sourceStr)){//星巢贷
                    sql = "select count(1) from app_order where  cust_No=?1 and (source ='" + sourceStr + "') and status = ?2 and (typ_grp <> '01' or pro_pur_amt > 0)";
                }else {
                    sql = "select count(1) from app_order where  cust_No=?1 and (channel_no ='" + sourceStr + "') and status = ?2 and (typ_grp <> '01' or pro_pur_amt > 0)";
                }
            }
        }
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, custNo);
        query.setParameter(2, status);
        return Integer.parseInt(query.getSingleResult().toString());
    }

    /**
     * 按用户查询某天订单明细
     *
     * @param crtUsr
     * @param date
     * @return
     */
    public List queryAppOrder(String crtUsr, String date, String idNo, String source) {
        String condition = "";
        String sql;
        if (date != null && !"".equals(date.trim())) {
            condition = " and apply_dt = ?2";
        }
        String sourceCondition;
        if (StringUtils.isEmpty(source) || "14".equals(source)) {//嗨付
            //嗨付可以查看集团大数据和美分期
            sourceCondition = " (source='2' OR  source = '3' OR (source='11' and  channel_no = '34') OR (source='11' and channel_no = '35')) ";
        } else {//星巢贷、美分期等
            if ("34".equals(source) || "35".equals(source)) {
                //集团大数据与嗨付、美分期与嗨付 互看
                sourceCondition = " (channel_no ='" + source + "' OR source='2' OR source = '3') ";
            } else {
                //只查自己渠道
                if ("16".equals(source)){//星巢贷
                    sourceCondition = " (source ='" + source + "')";
                }else {
                    sourceCondition = " (channel_no ='" + source + "')";
                }
            }
        }
        sql = "select order_no,apply_Amt,totalNormInt,totalFeeAmt,apply_Tnr,typ_Grp,apply_Dt,status,"
                + "(select goods_name from app_order_goods where order_no = app_order.order_no and rownum = 1) goods_name,"
                + "(select count(1) from app_order_goods where order_no = app_order.order_no) goods_count,"
                + "cust_name,apply_tnr_typ" + " from app_order where " + sourceCondition + " and (crt_usr=?1 or id_no=?2)" + condition
                + " and (typ_grp <> '01' or pro_pur_amt > 0)" + " and status not in ('2','4')"
                + "  order by create_time desc";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, crtUsr);
        query.setParameter(2, idNo);
        if (date != null && !"".equals(date.trim())) {
            query.setParameter(2, date);
        }
        return query.getResultList();
    }
}
