package com.fuwei.controller.finishstore;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fuwei.commons.Pager;
import com.fuwei.commons.Sort;
import com.fuwei.commons.SystemCache;
import com.fuwei.commons.SystemContextUtils;
import com.fuwei.controller.BaseController;
import com.fuwei.entity.Employee;
import com.fuwei.entity.Order;
import com.fuwei.entity.User;
import com.fuwei.entity.finishstore.FinishStoreIn;
import com.fuwei.entity.finishstore.FinishStoreInDetail;
import com.fuwei.entity.finishstore.FinishStoreOut;
import com.fuwei.entity.finishstore.FinishStoreOutDetail;
import com.fuwei.entity.finishstore.FinishStoreStock;
import com.fuwei.entity.finishstore.FinishStoreStockDetail;
import com.fuwei.entity.finishstore.PackingOrder;
import com.fuwei.entity.finishstore.PackingOrderDetail;
import com.fuwei.service.AuthorityService;
import com.fuwei.service.OrderService;
import com.fuwei.service.finishstore.FinishStoreOutDetailService;
import com.fuwei.service.finishstore.FinishStoreOutService;
import com.fuwei.service.finishstore.FinishStoreStockService;
import com.fuwei.service.finishstore.PackingOrderService;
import com.fuwei.util.DateTool;
import com.fuwei.util.SerializeTool;

@RequestMapping("/finishstore_out")
@Controller
public class FinishStoreOutController extends BaseController {
	
	@Autowired
	FinishStoreOutService finishStoreOutService;
	@Autowired
	FinishStoreOutDetailService finishStoreInDetailService;
	@Autowired
	FinishStoreStockService finishStoreStockService;
	@Autowired
	PackingOrderService packingOrderService;
	@Autowired
	OrderService orderService;
	@Autowired
	AuthorityService authorityService;

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView index(Integer page, String start_time, String end_time,
			String orderNumber, String sortJSON, HttpSession session,
			HttpServletRequest request) throws Exception {

		String lcode = "finishstore/index";
		Boolean hasAuthority = SystemCache.hasAuthority(session, lcode);
		if (!hasAuthority) {
			throw new PermissionDeniedDataAccessException("没有查看成品出库列表的权限", null);
		}

		Date start_time_d = DateTool.parse(start_time);
		Date end_time_d = DateTool.parse(end_time);
		Pager pager = new Pager();
		if (page != null && page > 0) {
			pager.setPageNo(page);
		}

		List<Sort> sortList = null;
		if (sortJSON != null) {
			sortList = SerializeTool.deserializeList(sortJSON, Sort.class);
		}
		if (sortList == null) {
			sortList = new ArrayList<Sort>();
		}
		Sort sort = new Sort();
		sort.setDirection("desc");
		sort.setProperty("created_at");
		sortList.add(sort);
		Sort sort2 = new Sort();
		sort2.setDirection("desc");
		sort2.setProperty("id");
		sortList.add(sort2);

		pager = finishStoreOutService.getListAndDetail(pager, start_time_d, end_time_d,
				orderNumber,null,null, sortList);
		
		request.setAttribute("start_time", start_time_d);
		request.setAttribute("end_time", end_time_d);
		request.setAttribute("orderNumber", orderNumber);
		request.setAttribute("pager", pager);
		return new ModelAndView("finishstore/out/index");
	}
	
	@RequestMapping(value = "/list/{OrderId}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView listbyorder(@PathVariable Integer OrderId,
			HttpSession session, HttpServletRequest request) throws Exception {
		if (OrderId == null) {
			throw new Exception("缺少订单ID");
		}
		String lcode = "finishstore/index";
		Boolean hasAuthority = SystemCache.hasAuthority(session, lcode);
		if (!hasAuthority) {
			throw new PermissionDeniedDataAccessException("没有查看成品出库单列表的权限", null);
		}
		List<FinishStoreOut> resultlist = finishStoreOutService.getList(OrderId);
		if (resultlist == null) {
			resultlist = new ArrayList<FinishStoreOut>();
		}
		request.setAttribute("resultlist", resultlist);
		request.setAttribute("orderId", OrderId);
		return new ModelAndView("finishstore/in/listbyorder");
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView add(String orderNumber, HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Order order =  orderService.get(orderNumber);
		 if(order == null){
			 throw new Exception("找不到订单号为" + orderNumber + "的订单");
		 }
		int orderId = order.getId();
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "finishstore/add";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if (!hasAuthority) {
			throw new PermissionDeniedDataAccessException("没有添加成品出库单的权限", null);
		}
		request.setAttribute("order", order);
		try {
			PackingOrder packingOrder = packingOrderService.getByOrderAndDetail(orderId);
			if(packingOrder == null){
				throw new Exception("该订单没有创建装箱单，请先创建装箱单 点击此处创建 <a href='packing_order/add/"+ orderId + "'>添加装箱单</a>");
			}
			request.setAttribute("packingOrder", packingOrder);
			Map<Integer,FinishStoreStockDetail> stockMap = finishStoreStockService.getStockMapByOrder(orderId);
			request.setAttribute("stockMap", stockMap);
			return new ModelAndView("finishstore/out/add");	
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> add(FinishStoreOut finishStoreOut, String details,HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "finishstore/add";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有添加成品出库单的权限", null);
		}
		try {	
			if(finishStoreOut.getOrderId() == null){
				throw new Exception("订单ID不能为空");
			}
			if(finishStoreOut.getPackingOrderId() == null){
				throw new Exception("装箱单号不能为空");
			}
			finishStoreOut.setCreated_at(DateTool.now());// 设置创建时间
			finishStoreOut.setUpdated_at(DateTool.now());// 设置更新时间
			finishStoreOut.setCreated_user(user.getId());// 设置创建人
			
			Order order = orderService.get(finishStoreOut.getOrderId());
			if(order == null){
				throw new Exception("不存在ID="+finishStoreOut.getOrderId()+"的订单" );
			}
			finishStoreOut.setCharge_employee(order.getCharge_employee());
			finishStoreOut.setCompany_productNumber(order.getCompany_productNumber());
			finishStoreOut.setName(order.getName());
			finishStoreOut.setOrderNumber(order.getOrderNumber());
			finishStoreOut.setCompanyId(order.getCompanyId());
			finishStoreOut.setCustomerId(order.getCustomerId());
			finishStoreOut.setImg(order.getImg());
			finishStoreOut.setImg_s(order.getImg_s());
			finishStoreOut.setImg_ss(order.getImg_ss());
			List<FinishStoreOutDetail> detaillist = SerializeTool
						.deserializeList(details,
								FinishStoreOutDetail.class);
			Iterator<FinishStoreOutDetail> iter = detaillist.iterator();
			while(iter.hasNext()){
				FinishStoreOutDetail detail = iter.next();
				if(detail.getCartons() == 0){
					iter.remove();
				}
				detail.setQuantity(detail.getCartons() * detail.getPer_carton_quantity());
				if(detail.getQuantity() == 0){
					iter.remove();
				}
			}
			if(detaillist==null || detaillist.size()<=0){
				throw new Exception("请至少填写一条出库明细");
			}
			finishStoreOut.setDetaillist(detaillist);		
			Integer tableOrderId = finishStoreOutService.add(finishStoreOut);
			return this.returnSuccess("id", tableOrderId);
			
		} catch (Exception e) {
			throw e;
		}
		
	}
	

	
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delete(@PathVariable int id,HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "finishstore/delete";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有删除成品出库单的权限", null);
		}
		int success = finishStoreOutService.remove(id);
		
		return this.returnSuccess();
		
	}
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
	@ResponseBody
	public FinishStoreOut get(@PathVariable int id, HttpSession session,HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		String lcode = "finishstore/index";
		Boolean hasAuthority = SystemCache.hasAuthority(session, lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有查看成品出库单详情的权限", null);
		}
		FinishStoreOut finishStoreOut = finishStoreOutService.getAndDetail(id);
		return finishStoreOut;
	}
	
	@RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView detail(@PathVariable Integer id, HttpSession session,
			HttpServletRequest request) throws Exception {
		if (id == null) {
			throw new Exception("缺少成品入库单ID");
		}
		String lcode = "finishstore/index";
		Boolean hasAuthority = SystemCache.hasAuthority(session, lcode);
		if (!hasAuthority) {
			throw new PermissionDeniedDataAccessException("没有查看成品出库单详情的权限", null);
		}	
		FinishStoreOut finishStoreOut = finishStoreOutService.getAndDetail(id);
		request.setAttribute("finishStoreOut", finishStoreOut);
		return new ModelAndView("finishstore/out/detail");
	}
	
	@RequestMapping(value = "/put/{finishStoreOutId}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView update(@PathVariable Integer finishStoreOutId,
			HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "finishstore/edit";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if (!hasAuthority) {
			throw new PermissionDeniedDataAccessException("没有编辑成品出库单的权限", null);
		}
		try {
			if(finishStoreOutId!=null){
				FinishStoreOut finishStoreOut = finishStoreOutService.getAndDetail(finishStoreOutId);
				request.setAttribute("finishStoreOut", finishStoreOut);
				Map<Integer,FinishStoreStockDetail> stockMap = finishStoreStockService.getStockMapByOrder(finishStoreOut.getOrderId());
				request.setAttribute("stockMap", stockMap);
				return new ModelAndView("finishstore/out/edit");
				
			}
			throw new Exception("缺少成品出库单ID");
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/put", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> update(FinishStoreOut finishStoreOut, String details,HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "finishstore/edit";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有编辑成品出库单的权限", null);
		}
		finishStoreOut.setUpdated_at(DateTool.now());
		
		Order order = orderService.get(finishStoreOut.getOrderId());
		if(order == null){
			throw new Exception("不存在ID="+finishStoreOut.getOrderId()+"的订单" );
		}
		finishStoreOut.setCharge_employee(order.getCharge_employee());
		finishStoreOut.setCompany_productNumber(order.getCompany_productNumber());
		finishStoreOut.setName(order.getName());
		finishStoreOut.setOrderNumber(order.getOrderNumber());
		finishStoreOut.setCompanyId(order.getCompanyId());
		finishStoreOut.setCustomerId(order.getCustomerId());
		finishStoreOut.setImg(order.getImg());
		finishStoreOut.setImg_s(order.getImg_s());
		finishStoreOut.setImg_ss(order.getImg_ss());
		
		List<FinishStoreOutDetail> detaillist = SerializeTool
				.deserializeList(details,
						FinishStoreOutDetail.class);
		Iterator<FinishStoreOutDetail> iter = detaillist.iterator();
		while(iter.hasNext()){
			FinishStoreOutDetail detail = iter.next();
			if(detail.getCartons() == 0){
				iter.remove();
			}
			detail.setQuantity(detail.getCartons() * detail.getPer_carton_quantity());
			if(detail.getQuantity() == 0){
				iter.remove();
			}
		}
		if(detaillist==null || detaillist.size()<=0){
			throw new Exception("请至少填写一条出库明细");
		}
		finishStoreOut.setDetaillist(detaillist);
		Integer id = finishStoreOutService.update(finishStoreOut);
		return this.returnSuccess("id", id);
		
	}
	
	@RequestMapping(value = "/print/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView print(@PathVariable Integer id, HttpSession session,
			HttpServletRequest request) throws Exception {
		if (id == null) {
			throw new Exception("缺少成品出库单ID");
		}
		String lcode = "finishstore/print";
		Boolean hasAuthority = SystemCache.hasAuthority(session, lcode);
		if (!hasAuthority) {
			throw new PermissionDeniedDataAccessException("没有查看打印成品出库单的权限", null);
		}	
		FinishStoreOut finishStoreOut = finishStoreOutService.getAndDetail(id);
		request.setAttribute("finishStoreOut", finishStoreOut);
		return new ModelAndView("finishstore/out/print");
	}

	@RequestMapping(value = "/scan", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView scan(HttpSession session, HttpServletRequest request)
			throws Exception {
		return new ModelAndView("finishstore/out/scan");
	}
	
	//某订单的成品生产进度
	@RequestMapping(value = "/actual_out/{orderId}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView actual_in(@PathVariable Integer orderId, HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if(orderId == null){
			throw new Exception("订单号不能为空");
		}
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "order/progress";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if (!hasAuthority) {
			throw new PermissionDeniedDataAccessException("没有查看订单成品生产进度的权限", null);
		}
		try {
			FinishStoreStock storeStock = finishStoreStockService.getAndDetail(orderId);
			request.setAttribute("storeStock", storeStock);
			return new ModelAndView("finishstore/out/actual_out");	
			
		} catch (Exception e) {
			throw e;
		}
	}
	
}
