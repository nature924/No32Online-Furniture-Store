




















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
 * 商品订单
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/goodsOrder")
public class GoodsOrderController {
    private static final Logger logger = LoggerFactory.getLogger(GoodsOrderController.class);

    @Autowired
    private GoodsOrderService goodsOrderService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private AddressService addressService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private YonghuService yonghuService;
@Autowired
private CartService cartService;
@Autowired
private GoodsCommentbackService goodsCommentbackService;



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
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = goodsOrderService.queryPage(params);

        //字典表数据转换
        List<GoodsOrderView> list =(List<GoodsOrderView>)page.getList();
        for(GoodsOrderView c:list){
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
        GoodsOrderEntity goodsOrder = goodsOrderService.selectById(id);
        if(goodsOrder !=null){
            //entity转view
            GoodsOrderView view = new GoodsOrderView();
            BeanUtils.copyProperties( goodsOrder , view );//把实体数据重构到view中

                //级联表
                AddressEntity address = addressService.selectById(goodsOrder.getAddressId());
                if(address != null){
                    BeanUtils.copyProperties( address , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setAddressId(address.getId());
                }
                //级联表
                GoodsEntity goods = goodsService.selectById(goodsOrder.getGoodsId());
                if(goods != null){
                    BeanUtils.copyProperties( goods , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setGoodsId(goods.getId());
                }
                //级联表
                YonghuEntity yonghu = yonghuService.selectById(goodsOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
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
    public R save(@RequestBody GoodsOrderEntity goodsOrder, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,goodsOrder:{}",this.getClass().getName(),goodsOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            goodsOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        goodsOrder.setInsertTime(new Date());
        goodsOrder.setCreateTime(new Date());
        goodsOrderService.insert(goodsOrder);
        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody GoodsOrderEntity goodsOrder, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,goodsOrder:{}",this.getClass().getName(),goodsOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(StringUtil.isEmpty(role))
//            return R.error(511,"权限为空");
//        else if("用户".equals(role))
//            goodsOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<GoodsOrderEntity> queryWrapper = new EntityWrapper<GoodsOrderEntity>()
            .eq("id",0)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        GoodsOrderEntity goodsOrderEntity = goodsOrderService.selectOne(queryWrapper);
        if(goodsOrderEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      goodsOrder.set
            //  }
            goodsOrderService.updateById(goodsOrder);//根据id更新
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
        goodsOrderService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<GoodsOrderEntity> goodsOrderList = new ArrayList<>();//上传的东西
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
                            GoodsOrderEntity goodsOrderEntity = new GoodsOrderEntity();
//                            goodsOrderEntity.setGoodsOrderUuidNumber(data.get(0));                    //订单号 要改的
//                            goodsOrderEntity.setAddressId(Integer.valueOf(data.get(0)));   //收获地址 要改的
//                            goodsOrderEntity.setGoodsId(Integer.valueOf(data.get(0)));   //商品 要改的
//                            goodsOrderEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            goodsOrderEntity.setBuyNumber(Integer.valueOf(data.get(0)));   //购买的数量 要改的
//                            goodsOrderEntity.setGoodsOrderTruePrice(data.get(0));                    //实付价格 要改的
//                            goodsOrderEntity.setGoodsOrderTypes(Integer.valueOf(data.get(0)));   //订单类型 要改的
//                            goodsOrderEntity.setGoodsOrderPaymentTypes(Integer.valueOf(data.get(0)));   //支付类型 要改的
//                            goodsOrderEntity.setInsertTime(date);//时间
//                            goodsOrderEntity.setCreateTime(date);//时间
                            goodsOrderList.add(goodsOrderEntity);


                            //把要查询是否重复的字段放入map中
                                //订单号
                                if(seachFields.containsKey("goodsOrderUuidNumber")){
                                    List<String> goodsOrderUuidNumber = seachFields.get("goodsOrderUuidNumber");
                                    goodsOrderUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> goodsOrderUuidNumber = new ArrayList<>();
                                    goodsOrderUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("goodsOrderUuidNumber",goodsOrderUuidNumber);
                                }
                        }

                        //查询是否重复
                         //订单号
                        List<GoodsOrderEntity> goodsOrderEntities_goodsOrderUuidNumber = goodsOrderService.selectList(new EntityWrapper<GoodsOrderEntity>().in("goods_order_uuid_number", seachFields.get("goodsOrderUuidNumber")));
                        if(goodsOrderEntities_goodsOrderUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(GoodsOrderEntity s:goodsOrderEntities_goodsOrderUuidNumber){
                                repeatFields.add(s.getGoodsOrderUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [订单号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        goodsOrderService.insertBatch(goodsOrderList);
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
        PageUtils page = goodsOrderService.queryPage(params);

        //字典表数据转换
        List<GoodsOrderView> list =(List<GoodsOrderView>)page.getList();
        for(GoodsOrderView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        GoodsOrderEntity goodsOrder = goodsOrderService.selectById(id);
            if(goodsOrder !=null){


                //entity转view
                GoodsOrderView view = new GoodsOrderView();
                BeanUtils.copyProperties( goodsOrder , view );//把实体数据重构到view中

                //级联表
                    AddressEntity address = addressService.selectById(goodsOrder.getAddressId());
                if(address != null){
                    BeanUtils.copyProperties( address , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setAddressId(address.getId());
                }
                //级联表
                    GoodsEntity goods = goodsService.selectById(goodsOrder.getGoodsId());
                if(goods != null){
                    BeanUtils.copyProperties( goods , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setGoodsId(goods.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(goodsOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
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
    public R add(@RequestBody GoodsOrderEntity goodsOrder, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,goodsOrder:{}",this.getClass().getName(),goodsOrder.toString());
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if("用户".equals(role)){
            GoodsEntity goodsEntity = goodsService.selectById(goodsOrder.getGoodsId());
            if(goodsEntity == null){
                return R.error(511,"查不到该物品");
            }
            // Double goodsNewMoney = goodsEntity.getGoodsNewMoney();

            if(false){
            }
            else if((goodsEntity.getGoodsKucunNumber() -goodsOrder.getBuyNumber())<0){
                return R.error(511,"购买数量不能大于库存数量");
            }
            else if(goodsEntity.getGoodsNewMoney() == null){
                return R.error(511,"物品价格不能为空");
            }

            //计算所获得积分
            Double buyJifen =0.0;
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            YonghuEntity yonghuEntity = yonghuService.selectById(userId);
            if(yonghuEntity == null)
                return R.error(511,"用户不能为空");
            if(yonghuEntity.getNewMoney() == null)
                return R.error(511,"用户金额不能为空");
            double balance = yonghuEntity.getNewMoney() - goodsEntity.getGoodsNewMoney()*goodsOrder.getBuyNumber();//余额
            if(balance<0)
                return R.error(511,"余额不够支付");
            goodsOrder.setGoodsOrderTypes(2); //设置订单状态为已支付
            goodsOrder.setGoodsOrderTruePrice(goodsEntity.getGoodsNewMoney()*goodsOrder.getBuyNumber()); //设置实付价格
            goodsOrder.setYonghuId(userId); //设置订单支付人id
            goodsOrder.setGoodsOrderPaymentTypes(1);
            goodsOrder.setInsertTime(new Date());
            goodsOrder.setCreateTime(new Date());
                goodsEntity.setGoodsKucunNumber( goodsEntity.getGoodsKucunNumber() -goodsOrder.getBuyNumber());
                goodsService.updateById(goodsEntity);
                goodsOrderService.insert(goodsOrder);//新增订单
            yonghuEntity.setNewMoney(balance);//设置金额
            yonghuService.updateById(yonghuEntity);
            return R.ok();
        }else{
            return R.error(511,"您没有权限支付订单");
        }
    }
    /**
     * 添加订单
     */
    @RequestMapping("/order")
    public R add(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("order方法:,,Controller:{},,params:{}",this.getClass().getName(),params.toString());
        String goodsOrderUuidNumber = String.valueOf(new Date().getTime());

        //获取当前登录用户的id
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        Integer addressId = Integer.valueOf(String.valueOf(params.get("addressId")));

        Integer goodsOrderPaymentTypes = Integer.valueOf(String.valueOf(params.get("goodsOrderPaymentTypes")));//支付类型

        String data = String.valueOf(params.get("goodss"));
        JSONArray jsonArray = JSON.parseArray(data);
        List<Map> goodss = JSON.parseObject(jsonArray.toString(), List.class);

        //获取当前登录用户的个人信息
        YonghuEntity yonghuEntity = yonghuService.selectById(userId);

        //当前订单表
        List<GoodsOrderEntity> goodsOrderList = new ArrayList<>();
        //商品表
        List<GoodsEntity> goodsList = new ArrayList<>();
        //购物车ids
        List<Integer> cartIds = new ArrayList<>();

        BigDecimal zhekou = new BigDecimal(1.0);

        //循环取出需要的数据
        for (Map<String, Object> map : goodss) {
           //取值
            Integer goodsId = Integer.valueOf(String.valueOf(map.get("goodsId")));//商品id
            Integer buyNumber = Integer.valueOf(String.valueOf(map.get("buyNumber")));//购买数量
            GoodsEntity goodsEntity = goodsService.selectById(goodsId);//购买的商品
            String id = String.valueOf(map.get("id"));
            if(StringUtil.isNotEmpty(id))
                cartIds.add(Integer.valueOf(id));

            //判断商品的库存是否足够
            if(goodsEntity.getGoodsKucunNumber() < buyNumber){
                //商品库存不足直接返回
                return R.error(goodsEntity.getGoodsName()+"的库存不足");
            }else{
                //商品库存充足就减库存
                goodsEntity.setGoodsKucunNumber(goodsEntity.getGoodsKucunNumber() - buyNumber);
            }

            //订单信息表增加数据
            GoodsOrderEntity goodsOrderEntity = new GoodsOrderEntity<>();

            //赋值订单信息
            goodsOrderEntity.setGoodsOrderUuidNumber(goodsOrderUuidNumber);//订单号
            goodsOrderEntity.setAddressId(addressId);//收获地址
            goodsOrderEntity.setGoodsId(goodsId);//商品
            goodsOrderEntity.setYonghuId(userId);//用户
            goodsOrderEntity.setBuyNumber(buyNumber);//购买的数量 ？？？？？？
            goodsOrderEntity.setGoodsOrderTypes(2);//订单类型
            goodsOrderEntity.setGoodsOrderPaymentTypes(goodsOrderPaymentTypes);//支付类型
            goodsOrderEntity.setInsertTime(new Date());//订单创建时间
            goodsOrderEntity.setCreateTime(new Date());//创建时间

            //判断是什么支付方式 1代表余额 2代表积分
            if(goodsOrderPaymentTypes == 1){//余额支付
                //计算金额
                Double money = new BigDecimal(goodsEntity.getGoodsNewMoney()).multiply(new BigDecimal(buyNumber)).multiply(zhekou).doubleValue();

                if(yonghuEntity.getNewMoney() - money <0 ){
                    return R.error("余额不足,请充值！！！");
                }else{
                    //计算所获得积分
                    Double buyJifen =0.0;
                    yonghuEntity.setNewMoney(yonghuEntity.getNewMoney() - money); //设置金额


                    goodsOrderEntity.setGoodsOrderTruePrice(money);

                }
            }
            goodsOrderList.add(goodsOrderEntity);
            goodsList.add(goodsEntity);

        }
        goodsOrderService.insertBatch(goodsOrderList);
        goodsService.updateBatchById(goodsList);
        yonghuService.updateById(yonghuEntity);
        if(cartIds != null && cartIds.size()>0)
            cartService.deleteBatchIds(cartIds);
        return R.ok();
    }






    /**
    * 退款
    */
    @RequestMapping("/refund")
    public R refund(Integer id, HttpServletRequest request){
        logger.debug("refund方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        String role = String.valueOf(request.getSession().getAttribute("role"));

        if("用户".equals(role)){
            GoodsOrderEntity goodsOrder = goodsOrderService.selectById(id);
            Integer buyNumber = goodsOrder.getBuyNumber();
            Integer goodsOrderPaymentTypes = goodsOrder.getGoodsOrderPaymentTypes();
            Integer goodsId = goodsOrder.getGoodsId();
            if(goodsId == null)
                return R.error(511,"查不到该物品");
            GoodsEntity goodsEntity = goodsService.selectById(goodsId);
            if(goodsEntity == null)
                return R.error(511,"查不到该物品");
            Double goodsNewMoney = goodsEntity.getGoodsNewMoney();
            if(goodsNewMoney == null)
                return R.error(511,"物品价格不能为空");

            Integer userId = (Integer) request.getSession().getAttribute("userId");
            YonghuEntity yonghuEntity = yonghuService.selectById(userId);
            if(yonghuEntity == null)
                return R.error(511,"用户不能为空");
            if(yonghuEntity.getNewMoney() == null)
                return R.error(511,"用户金额不能为空");

            Double zhekou = 1.0;


            //判断是什么支付方式 1代表余额 2代表积分
            if(goodsOrderPaymentTypes == 1){//余额支付
                //计算金额
                Double money = goodsEntity.getGoodsNewMoney() * buyNumber  * zhekou;
                //计算所获得积分
                Double buyJifen = 0.0;
                yonghuEntity.setNewMoney(yonghuEntity.getNewMoney() + money); //设置金额


            }

            goodsEntity.setGoodsKucunNumber(goodsEntity.getGoodsKucunNumber() + buyNumber);



            goodsOrder.setGoodsOrderTypes(1);//设置订单状态为退款
            goodsOrderService.updateById(goodsOrder);//根据id更新
            yonghuService.updateById(yonghuEntity);//更新用户信息
            goodsService.updateById(goodsEntity);//更新订单中物品的信息
            return R.ok();
        }else{
            return R.error(511,"您没有权限退款");
        }
    }


    /**
     * 发货
     */
    @RequestMapping("/deliver")
    public R deliver(Integer id){
        logger.debug("refund:,,Controller:{},,ids:{}",this.getClass().getName(),id.toString());
        GoodsOrderEntity  goodsOrderEntity = new  GoodsOrderEntity();;
        goodsOrderEntity.setId(id);
        goodsOrderEntity.setGoodsOrderTypes(3);
        boolean b =  goodsOrderService.updateById( goodsOrderEntity);
        if(!b){
            return R.error("发货出错");
        }
        return R.ok();
    }









    /**
     * 收货
     */
    @RequestMapping("/receiving")
    public R receiving(Integer id){
        logger.debug("refund:,,Controller:{},,ids:{}",this.getClass().getName(),id.toString());
        GoodsOrderEntity  goodsOrderEntity = new  GoodsOrderEntity();
        goodsOrderEntity.setId(id);
        goodsOrderEntity.setGoodsOrderTypes(4);
        boolean b =  goodsOrderService.updateById( goodsOrderEntity);
        if(!b){
            return R.error("收货出错");
        }
        return R.ok();
    }



    /**
    * 评价
    */
    @RequestMapping("/commentback")
    public R commentback(Integer id, String commentbackText,HttpServletRequest request){
        logger.debug("commentback方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if("用户".equals(role)){
            GoodsOrderEntity goodsOrder = goodsOrderService.selectById(id);
        if(goodsOrder == null)
            return R.error(511,"查不到该订单");
        if(goodsOrder.getGoodsOrderTypes() != 4)
            return R.error(511,"您不能评价");
        Integer goodsId = goodsOrder.getGoodsId();
        if(goodsId == null)
            return R.error(511,"查不到该物品");

        GoodsCommentbackEntity goodsCommentbackEntity = new GoodsCommentbackEntity();
            goodsCommentbackEntity.setId(id);
            goodsCommentbackEntity.setGoodsId(goodsId);
            goodsCommentbackEntity.setYonghuId((Integer) request.getSession().getAttribute("userId"));
            goodsCommentbackEntity.setGoodsCommentbackText(commentbackText);
            goodsCommentbackEntity.setReplyText(null);
            goodsCommentbackEntity.setInsertTime(new Date());
            goodsCommentbackEntity.setUpdateTime(null);
            goodsCommentbackEntity.setCreateTime(new Date());
            goodsCommentbackService.insert(goodsCommentbackEntity);

            goodsOrder.setGoodsOrderTypes(5);//设置订单状态为已评价
            goodsOrderService.updateById(goodsOrder);//根据id更新
            return R.ok();
        }else{
            return R.error(511,"您没有权限评价");
        }
    }












}
