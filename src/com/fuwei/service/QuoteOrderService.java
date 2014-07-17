package com.fuwei.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fuwei.entity.Quote;
import com.fuwei.entity.QuoteOrder;


@Component
public class QuoteOrderService extends BaseService {
	@Autowired
	JdbcTemplate jdbc;
	
	@Autowired
	QuoteOrderDetailService  quoteOrderDetailService;
	// 获取报价单列表
	public List<QuoteOrder> getList() throws Exception {
		try {
			List<QuoteOrder> quoteorderList = dao.queryForBeanList(
					"SELECT * FROM tb_quote_order", QuoteOrder.class);
			return quoteorderList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	// 添加报价单
	@Transactional
	public int add(QuoteOrder quoteorder) throws Exception {
		try{
			if(quoteorder.getDetaillist()==null || quoteorder.getDetaillist().size()<=0){
				throw new Exception("报价单中至少得有一条报价记录");
			}else{
				quoteOrderDetailService.addBatch(quoteorder.getDetaillist());
			}
			return this.insert(quoteorder);
		}catch(Exception e){
			throw e;
		}
	}
	

//	// 删除报价
//	public int remove(int id) throws Exception {
//		try{
//			return dao.update("delete from tb_quote WHERE  id = ?", id);
//		}catch(Exception e){
//			throw e;
//		}
//	}
//
//	// 编辑报价
//	public int update(Quote quote) throws Exception {
//		try{
//			//UPDATE tb_user SET inUse = true WHERE  id = ?
//			return this.update(quote, "id", null,true);
//		}catch(Exception e){
//			throw e;
//		}
//	}
//	
//	// 获取报价
//	public Quote get(int id) throws Exception {
//		try {
//			Quote quote = dao.queryForBean(
//					"select * from tb_quote where id = ?", Quote.class,
//					id);
//			return quote;
//		} catch (Exception e) {
//			throw e;
//		}
//	}
}