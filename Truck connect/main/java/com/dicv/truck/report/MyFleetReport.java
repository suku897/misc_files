package com.dicv.truck.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.dicv.truck.dto.MyFleetVehicleReport;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJHyperLink;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.StringExpression;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.builders.StyleBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class MyFleetReport {

	private final ArrayList<MyFleetVehicleReport> list = new ArrayList<MyFleetVehicleReport>();

	public MyFleetReport(Collection<MyFleetVehicleReport> c) {
		list.addAll(c);
	}

	public JasperPrint getReport() throws ColumnBuilderException, JRException, ClassNotFoundException {
		Style headerStyle = ReportLayout.createHeaderStyle();
		Style detailTextStyle = ReportLayout.createDetailTextStyle();
		Style detailNumberStyle = ReportLayout.createDetailNumberStyle();
		DynamicReport dynaReport = getReport(headerStyle, detailTextStyle, detailNumberStyle);
		JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dynaReport, new ClassicLayoutManager(),
				new JRBeanCollectionDataSource(list));
		return jp;
	}

	public AbstractColumn createColumn(String property, Class<?> type, String title, Style headerStyle,
			Style detailStyle, int width) throws ColumnBuilderException {
		AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(property, type.getName()).setTitle(title)
				.setStyle(detailStyle).setHeaderStyle(headerStyle).setWidth(Integer.valueOf(width)).setFixedWidth(true)
				.build();
		return columnState;

	}

	private DynamicReport getReport(Style headerStyle, Style detailTextStyle, Style detailNumStyle)
			throws ColumnBuilderException, ClassNotFoundException {

		FastReportBuilder report = new FastReportBuilder();

		AbstractColumn registrationId = createColumn("registrationId", String.class, "Registration Number", headerStyle,
				detailTextStyle, 180);
		AbstractColumn runningStatus = createColumn("runningStatus", String.class, "Running Status", headerStyle,
				detailTextStyle, 140);
		AbstractColumn gpsSpkm = createColumn("gpsSpkm", Integer.class, "Speed (Kmph)", headerStyle, detailTextStyle,
				140);

		AbstractColumn gpsTime = createColumn("gpsTime", String.class, "Measured At", headerStyle, detailTextStyle,
				160);
		AbstractColumn defaultDriver = createColumn("defaultDriver", String.class, "Driver", headerStyle,
				detailTextStyle, 140);
		AbstractColumn currentLocation = createColumn("currentLocation", String.class, "Location", headerStyle,
				ReportLayout.createLinkStyle(), 400);

		DJHyperLink djlink2 = new DJHyperLink();
		djlink2.setExpression(new StringExpression() {
			public Object evaluate(Map fields, Map variables, Map parameters) {
				return list.get(Integer.parseInt(variables.get("REPORT_COUNT").toString()) - 1).getLatLong();

			}
		});
		report.setReportName("MyFleetVehicleReport").addColumn(registrationId).addColumn(runningStatus)
				.addColumn(gpsSpkm).addColumn(currentLocation).addColumn(gpsTime).addColumn(defaultDriver)
				.setUseFullPageWidth(true);
		currentLocation.setLink(djlink2);
		currentLocation.setStyle(ReportLayout.createLinkStyle());
		StyleBuilder titleStyle = new StyleBuilder(true);
		titleStyle.setHorizontalAlign(HorizontalAlign.CENTER);
		titleStyle.setFont(new Font(20, Font._FONT_GEORGIA, true));
		StyleBuilder subTitleStyle = new StyleBuilder(true);
		subTitleStyle.setHorizontalAlign(HorizontalAlign.CENTER);
		subTitleStyle.setFont(new Font(Font.MEDIUM, Font._FONT_GEORGIA, true));
		report.setTitleStyle(titleStyle.build());
		report.setUseFullPageWidth(true);
		report.setIgnorePagination(true);
		return report.build();
	}

}
