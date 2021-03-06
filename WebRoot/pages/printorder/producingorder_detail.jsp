<%@ page language="java" import="java.util.*" pageEncoding="utf-8"
	contentType="text/html; charset=utf-8"%>
<%@page import="com.fuwei.entity.OrderDetail"%>
<%@page import="com.fuwei.entity.Order"%>
<%@page import="com.fuwei.entity.ordergrid.ProducingOrder"%>
<%@page import="com.fuwei.entity.ordergrid.ProducingOrderDetail"%>
<%@page import="com.fuwei.entity.ordergrid.ProducingOrderMaterialDetail"%>
<%@page import="com.fuwei.commons.SystemCache"%>
<%@page import="com.fuwei.util.SerializeTool"%>
<%@page import="com.fuwei.util.DateTool"%>
<%
	//Order order = (Order) request.getAttribute("order");
	List<ProducingOrder> producingOrderList = (List<ProducingOrder>) request
			.getAttribute("producingOrderList");
	producingOrderList = producingOrderList == null? new ArrayList<ProducingOrder>()
			: producingOrderList;
	String productfactoryStr = (String)request.getAttribute("productfactoryStr");
	Boolean has_order_producing_price = SystemCache.hasAuthority(session,
			"order/producing/price");
%>
<!DOCTYPE html>
<html>
	<head>

		<title>打印生产单 -- 桐庐富伟针织厂</title>
		<meta charset="utf-8">
		<meta http-equiv="keywords" content="针织厂,针织,富伟,桐庐">
		<meta http-equiv="description" content="富伟桐庐针织厂">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<script src="js/plugins/jquery-barcode.min.js"></script>
		<!-- 为了让IE浏览器运行最新的渲染模式 -->
	</head>
	<body>

		<%for(ProducingOrder producingOrder : producingOrderList){ 
			List<ProducingOrderDetail> producingOrderDetailList = producingOrder == null ? new ArrayList<ProducingOrderDetail>()
			: producingOrder.getDetaillist();
			List<ProducingOrderMaterialDetail> producingOrderMaterialDetailList = producingOrder == null ? new ArrayList<ProducingOrderMaterialDetail>()
			: producingOrder.getDetail_2_list();
		%>
		<div style="page-break-after: always">
		<div class="container-fluid gridTab auto_container">
			<div class="row">
				<div class="col-md-12 tablewidget">
					<table class="table noborder">
						<caption id="tablename">
							桐庐富伟针织厂生产单<div table_id="<%=producingOrder.getNumber() %>" class="id_barcode"></div>
						</caption>
						<tr><td colspan="3" class="pull-right">№：<%=producingOrder.getOrderNumber() %> - <%=producingOrder.getNumber() %></td></tr>
					</table>

					<table id="orderTb" class="tableTb">
						<tbody>
							<tr>
								<td align="center" rowspan="7" width="50%">
									<img id="previewImg" alt="200 x 100%"
										src="/<%=producingOrder.getImg_s()%>">
								</td>
								<td width="20%">
									生产单位
								</td>
								<td class="orderproperty"><%=producingOrder.getFactoryId() == null ? "" : SystemCache
					.getFactoryName(producingOrder.getFactoryId())%></td>
							</tr>

							<tr>
								<td colspan="2" class="center">
									订单信息
								</td>
							</tr>
							<tr>
								<td>
									公司
								</td>
								<td><%=SystemCache.getCompanyShortName(producingOrder.getCompanyId())%></td>
							</tr>
							<tr>
								<td>
									客户
								</td>
								<td><%=SystemCache.getCustomerName(producingOrder.getCustomerId())%></td>
							</tr>
							<tr>
								<td>
									货号
								</td>
								<td><%=producingOrder.getCompany_productNumber()%></td>
							</tr>
							<tr>
								<td>
									款名
								</td>
								<td><%=producingOrder.getName()%></td>
							</tr>
							<tr>
								<td>
									跟单
								</td>
								<td><%=SystemCache.getEmployeeName(producingOrder.getCharge_employee())%></td>
							</tr>
						</tbody>
					</table>

					<table id="mainTb" class="noborder">
						<tr>
							<td>
								<table class="detailTb">
									<caption>
										颜色及数量
									</caption>
									<thead>
										<tr>
											<th width="15%">
												颜色
											</th>
											<th width="15%">
												克重(g)
											</th>
											<th width="15%">
												纱线种类
											</th>
											<th width="15%">
												尺寸
											</th>
											<th width="15%">
												生产数量
											</th>
											<%if(has_order_producing_price){ %>
											<th width="15%">
												价格(/个)
											</th>
											<%} %>

										</tr>
									</thead>
									<tbody>
										<%
											for (ProducingOrderDetail detail : producingOrderDetailList) {
										%>
										<tr class="tr">
											<td class="color"><%=detail.getColor()%>
											</td>
											<td class="product_weight"><%=detail.getProduce_weight()%>
											</td>
											<td class="yarn_name"><%=SystemCache.getMaterialName(detail.getYarn()) %>
											</td>
											<td class="size"><%=detail.getSize()%>
											</td>
											<td class="quantity"><%=detail.getQuantity()%>
											</td>
											<%if(has_order_producing_price){ %>
											<td class="price"><%=detail.getPrice()%>
											</td>
											<%} %>
										</tr>

										<%
											}
										%>

									</tbody>
								</table>
							</td>
						</tr>
						<tr>
							<td>
								<table class="detailTb2">
									<caption>
										生产材料信息
									</caption>
									<thead>
										<tr>
											<th width="20%">
												材料
											</th>
											<th width="20%">
												色号
											</th>
											<th width="20%">
												数量(kg)
											</th>
											<th width="25%">
												标准色样
											</th>
										</tr>
									</thead>
									<tbody>
										<%
											for (ProducingOrderMaterialDetail detail : producingOrderMaterialDetailList) {
										%>
										<tr class="tr">
											<td class="material_name"><%=SystemCache.getMaterialName(detail.getMaterial()) %>
											</td>
											<td class="color"><%=detail.getColor()%>
											</td>
											<td class="quantity"><%=detail.getQuantity()%>
											</td>
											<td class="colorsample"><%=detail.getColorsample()%>
											</td>
										</tr>

										<%
											}
										%>

									</tbody>
								</table>
							</td>
						</tr>
						<tr>
							<td>
								<table class="auto_height">
									<caption>

									</caption>
									<tbody>
										<tr>
											<td></td>
										</tr>
									</tbody>
									<tfoot>
										<tr>
											<td>
												<p class="pull-right" id="sign">
													是否同意生产：
												</p>
											</td>
										</tr>
									</tfoot>
								</table>
							</td>
						</tr>
					</table>


					<p id="tip" class="auto_bottom">
						各加工单位：请严格按照此生产单信息进行相关生产。
						<strong>生产前须由我厂质量管理封样，我方确认同意生产并签字后方可生产大货，否则一切后果由贵单位承担。</strong>此生产单一式两份，确认后请签字。此生产单妥善保管，结账以此单为准。
					</p>
					
					<div style="padding-bottom: 5px;" class="tip_line">
						<span style="font-weight: bold;text-decoration: underline;font-size: 18px;">请妥善保管此单，结账时须出具本生产单及相应半成品入库单方可结账。</span>
					</div>
					<p class="pull-right auto_bottom">
						<span id="created_user">制单人：<%=SystemCache.getUserName(producingOrder.getCreated_user()) %></span>
						<span id="date"> 日期：<%=DateTool.formatDateYMD(DateTool.getYanDate(producingOrder.getCreated_at())) %></span>
					</p>



				</div>

			</div>
		</div>
		</div>
		<%} %>
	</body>
	<script type="text/javascript">
		$(".id_barcode").each(function(){
			var id =$(this).attr("table_id");
			$(this).barcode(id, "code128",{barWidth:2, barHeight:30,showHRI:false});
		});
		
	</script>
</html>