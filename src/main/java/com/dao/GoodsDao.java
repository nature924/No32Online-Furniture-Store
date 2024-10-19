package com.dao;

import com.entity.GoodsEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.view.GoodsView;

/**
 * 商品信息 Dao 接口
 *
 * @author 
 */
public interface GoodsDao extends BaseMapper<GoodsEntity> {

   List<GoodsView> selectListView(Pagination page,@Param("params")Map<String,Object> params);

}
