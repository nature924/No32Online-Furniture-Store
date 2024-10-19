





















package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 商品信息
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static final Logger logger = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private GoodsService goodsService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        params.put("goodsDeleteStart",1);params.put("goodsDeleteEnd",1);
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = goodsService.queryPage(params);

        //字典表数据转换
        List<GoodsView> list =(List<GoodsView>)page.getList();
        for(GoodsView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        GoodsEntity goods = goodsService.selectById(id);
        if(goods !=null){
            //entity转view
            GoodsView view = new GoodsView();
            BeanUtils.copyProperties( goods , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody GoodsEntity goods, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,goods:{}",this.getClass().getName(),goods.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");

        Wrapper<GoodsEntity> queryWrapper = new EntityWrapper<GoodsEntity>()
            .eq("goods_name", goods.getGoodsName())
            .eq("goods_types", goods.getGoodsTypes())
            .eq("goods_kucun_number", goods.getGoodsKucunNumber())
            .eq("goods_clicknum", goods.getGoodsClicknum())
            .eq("shangxia_types", goods.getShangxiaTypes())
            .eq("goods_delete", goods.getGoodsDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        GoodsEntity goodsEntity = goodsService.selectOne(queryWrapper);
        if(goodsEntity==null){
            goods.setGoodsClicknum(1);
            goods.setShangxiaTypes(1);
            goods.setGoodsDelete(1);
            goods.setCreateTime(new Date());
            goodsService.insert(goods);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody GoodsEntity goods, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,goods:{}",this.getClass().getName(),goods.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(StringUtil.isEmpty(role))
//            return R.error(511,"权限为空");
        //根据字段查询是否有相同数据
        Wrapper<GoodsEntity> queryWrapper = new EntityWrapper<GoodsEntity>()
            .notIn("id",goods.getId())
            .andNew()
            .eq("goods_name", goods.getGoodsName())
            .eq("goods_types", goods.getGoodsTypes())
            .eq("goods_kucun_number", goods.getGoodsKucunNumber())
            .eq("goods_clicknum", goods.getGoodsClicknum())
            .eq("shangxia_types", goods.getShangxiaTypes())
            .eq("goods_delete", goods.getGoodsDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        GoodsEntity goodsEntity = goodsService.selectOne(queryWrapper);
        if("".equals(goods.getGoodsPhoto()) || "null".equals(goods.getGoodsPhoto())){
                goods.setGoodsPhoto(null);
        }
        if(goodsEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      goods.set
            //  }
            goodsService.updateById(goods);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        ArrayList<GoodsEntity> list = new ArrayList<>();
        for(Integer id:ids){
            GoodsEntity goodsEntity = new GoodsEntity();
            goodsEntity.setId(id);
            goodsEntity.setGoodsDelete(2);
            list.add(goodsEntity);
        }
        if(list != null && list.size() >0){
            goodsService.updateBatchById(list);
        }
        return R.ok();
    }

    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<GoodsEntity> goodsList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            GoodsEntity goodsEntity = new GoodsEntity();
//                            goodsEntity.setGoodsName(data.get(0));                    //商品名称 要改的
//                            goodsEntity.setGoodsTypes(Integer.valueOf(data.get(0)));   //商品类型 要改的
//                            goodsEntity.setGoodsPhoto("");//照片
//                            goodsEntity.setGoodsKucunNumber(Integer.valueOf(data.get(0)));   //商品库存 要改的
//                            goodsEntity.setGoodsOldMoney(data.get(0));                    //商品原价 要改的
//                            goodsEntity.setGoodsNewMoney(data.get(0));                    //现价 要改的
//                            goodsEntity.setGoodsClicknum(Integer.valueOf(data.get(0)));   //点击次数 要改的
//                            goodsEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否上架 要改的
//                            goodsEntity.setGoodsDelete(1);//逻辑删除字段
//                            goodsEntity.setGoodsContent("");//照片
//                            goodsEntity.setCreateTime(date);//时间
                            goodsList.add(goodsEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        goodsService.insertBatch(goodsList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = goodsService.queryPage(params);

        //字典表数据转换
        List<GoodsView> list =(List<GoodsView>)page.getList();
        for(GoodsView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        GoodsEntity goods = goodsService.selectById(id);
            if(goods !=null){

                //点击数量加1
                goods.setGoodsClicknum(goods.getGoodsClicknum()+1);
                goodsService.updateById(goods);

                //entity转view
                GoodsView view = new GoodsView();
                BeanUtils.copyProperties( goods , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody GoodsEntity goods, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,goods:{}",this.getClass().getName(),goods.toString());
        Wrapper<GoodsEntity> queryWrapper = new EntityWrapper<GoodsEntity>()
            .eq("goods_name", goods.getGoodsName())
            .eq("goods_types", goods.getGoodsTypes())
            .eq("goods_kucun_number", goods.getGoodsKucunNumber())
            .eq("goods_clicknum", goods.getGoodsClicknum())
            .eq("shangxia_types", goods.getShangxiaTypes())
            .eq("goods_delete", goods.getGoodsDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        GoodsEntity goodsEntity = goodsService.selectOne(queryWrapper);
        if(goodsEntity==null){
            goods.setGoodsDelete(1);
            goods.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      goods.set
        //  }
        goodsService.insert(goods);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


}
