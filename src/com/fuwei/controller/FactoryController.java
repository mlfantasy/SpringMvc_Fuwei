package com.fuwei.controller;

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

import com.fuwei.commons.SystemCache;
import com.fuwei.commons.SystemContextUtils;
import com.fuwei.entity.Company;
import com.fuwei.entity.Factory;
import com.fuwei.entity.User;
import com.fuwei.service.AuthorityService;
import com.fuwei.service.CompanyService;
import com.fuwei.service.FactoryService;
import com.fuwei.util.DateTool;
import com.fuwei.util.HanyuPinyinUtil;

@RequestMapping("/factory")
@Controller
public class FactoryController extends BaseController {
	@Autowired
	FactoryService factoryService;
	@Autowired
	AuthorityService authorityService;
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> add(Factory factory,HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "factory/add";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有添加加工工厂的权限", null);
		}
		factory.setHelp_code(HanyuPinyinUtil.getFirstSpellByString(factory.getName())) ;
		factory.setCreated_at(DateTool.now());
		factory.setUpdated_at(DateTool.now());
		factory.setCreated_user(user.getId());
		int success = factoryService.add(factory);
		
		//更新缓存
		new SystemCache().initFactoryList();
		
		return this.returnSuccess();
		
	}
	
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delete(@PathVariable int id,HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "factory/delete";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有删除加工工厂的权限", null);
		}
		int success = factoryService.remove(id);
		
		//更新缓存
		new SystemCache().initFactoryList();
		
		return this.returnSuccess();
		
	}
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Factory get(@PathVariable int id, HttpSession session,HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		String lcode = "factory/index";
		Boolean hasAuthority = SystemCache.hasAuthority(session, lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有查看加工工厂列表的权限", null);
		}
		Factory Factory = factoryService.get(id);
		return Factory;
	}
	
	@RequestMapping(value = "/put", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> update(Factory Factory,HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		User user = SystemContextUtils.getCurrentUser(session).getLoginedUser();
		String lcode = "factory/edit";
		Boolean hasAuthority = authorityService.checkLcode(user.getId(), lcode);
		if(!hasAuthority){
			throw new PermissionDeniedDataAccessException("没有编辑加工工厂的权限", null);
		}
		Factory.setHelp_code(HanyuPinyinUtil.getFirstSpellByString(Factory.getName())) ;
		Factory.setUpdated_at(DateTool.now());
		int success = factoryService.update(Factory);
		
		//更新缓存
		new SystemCache().initFactoryList();
		
		return this.returnSuccess();
		
	}
}
