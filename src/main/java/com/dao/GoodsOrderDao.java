package com.dao;

import com.entity.GoodsOrderEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.view.GoodsOrderView;

/**
 * 商品订单 Dao 接口
 *
 * @author 
 */
public interface GoodsOrderDao extends BaseMapper<GoodsOrderEntity> {

   List<GoodsOrderView> selectListView(Pagination page,@Param("params")Map<String,Object> params);

}
