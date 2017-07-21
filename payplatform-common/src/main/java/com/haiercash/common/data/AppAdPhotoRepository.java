package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppAdPhotoRepository extends JpaRepository<AppAdPhoto, String> {
    @Query(value="select * from  APP_AD_Photo t where  AD_ID = ?1  and SIZE_TYPE = ?2  AND img_type='DESC' AND  ROWNUM=1 " ,nativeQuery = true)
    AppAdPhoto findByadIdAndSizeType(String adId, String sizeType);

    @Query(value="select * from  APP_AD_Photo t where  AD_ID = ?1  and SIZE_TYPE = ?2  " ,nativeQuery = true)
    List<AppAdPhoto> findAllByadIdAndSizeType(String adId, String sizeType);
    /**
     * 查询开屏广告（只有一个） 条件：isActive 为Y,isSplash为Y, rowNum为1, imgType为SHOW, sizeType为传入参数
     * @param sizeType
     * @return
     */
    @Query(value="select  a.id adid,a.show_time,p.id  photoId ,p.size_type,p.display_height,p.display_width  from app_ad_info a,app_ad_photo p where a.id=p.ad_id and a.is_active='Y' and a.is_splash='Y'and rownum=1 and p.img_type='SHOW' and p.size_type=?1",nativeQuery = true)
    List<Object[]> findKpAdInfo(String sizeType);
    /**
     * 查询焦点广告 条件：isActive 为Y, imgType为FOCUS, sizeType为传入参数
     * @param sizeType
     * @return
     */
    @Query(value="select a.id adid,a.show_time,p.id  photoId ,p.size_type,p.display_height,p.display_width  from app_ad_info a,app_ad_photo p where a.id=p.ad_id and a.is_active='Y' and p.img_type='FOCUS' and p.size_type=?1",nativeQuery = true)
    List<Object[]> findJdAdBySizeType(String sizeType);
}
