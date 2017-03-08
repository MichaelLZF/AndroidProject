package com.lzf.ffcontacts.utlis;

import android.content.ContentValues;
import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
	public static WritableFont arial14font = null;
	public static WritableCellFormat arial14format = null;
	public static WritableFont arial10font = null;
	public static WritableCellFormat arial10format = null;
	public static WritableFont arial12font = null;
	public static WritableCellFormat arial12format = null;

	public final static String UTF8_ENCODING = "UTF-8";
	public final static String GBK_ENCODING = "GBK";
	//格式
	public static void format() {
		try {
			arial14font = new WritableFont(WritableFont.ARIAL, 14,
					WritableFont.BOLD);
			arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
			arial14format = new WritableCellFormat(arial14font);
			arial14format.setAlignment(jxl.format.Alignment.CENTRE);
			arial14format.setBorder(jxl.format.Border.ALL,
					jxl.format.BorderLineStyle.THIN);
			arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

			arial10font = new WritableFont(WritableFont.ARIAL, 10,
					WritableFont.BOLD);
			arial10format = new WritableCellFormat(arial10font);
			arial10format.setAlignment(jxl.format.Alignment.CENTRE);
			arial10format.setBorder(jxl.format.Border.ALL,
					jxl.format.BorderLineStyle.THIN);
			arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);

			arial12font = new WritableFont(WritableFont.ARIAL, 12);
			arial12format = new WritableCellFormat(arial12font);
			arial12format.setBorder(jxl.format.Border.ALL,
					jxl.format.BorderLineStyle.THIN);
		} catch (WriteException e) {

			e.printStackTrace();
		}
	}
	//
	public static void initExcel(String fileName, String[] colName) {
		format();
		WritableWorkbook workbook = null;
		try {
			File file = new File(fileName);
			//创建文件夹
			if (!file.exists()) {
				file.createNewFile();
			}
			//创建文件，即excel
			workbook = Workbook.createWorkbook(file);
			WritableSheet sheet = workbook.createSheet("葫芦娃联系人表", 0);
			sheet.addCell((WritableCell) new Label(0, 0, fileName,
					arial14format));
			for (int col = 0; col < colName.length; col++) {
				sheet.addCell(new Label(col, 0, colName[col], arial10format));
			}
			workbook.write();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> void writeObjListToExcel(List<T> objList,
											   String fileName, Context c) {
		if (objList != null && objList.size() > 0) {
			WritableWorkbook writebook = null;
			InputStream in = null;
			try {
				WorkbookSettings setEncode = new WorkbookSettings();
				setEncode.setEncoding(UTF8_ENCODING);
				in = new FileInputStream(new File(fileName));
				Workbook workbook = Workbook.getWorkbook(in);
				writebook = Workbook.createWorkbook(new File(fileName),
						workbook);
				WritableSheet sheet = writebook.getSheet(0);
				for (int j = 0; j < objList.size(); j++) {
					ArrayList<String> list = (ArrayList<String>) objList.get(j);
					for (int i = 0; i < list.size(); i++) {
						sheet.addCell(new Label(i, j + 1, list.get(i),
								arial12format));
					}
				}
				writebook.write();
				Toast.makeText(c, "导出到手机存储中文件夹FFContacts成功", Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (writebook != null) {
					try {
						writebook.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static List<PersonInfo> read2DB(File f, Context con) {
		ArrayList<PersonInfo> billList = new ArrayList<PersonInfo>();
		try {
			Workbook course = null;
			course = Workbook.getWorkbook(f);
			Sheet sheet = course.getSheet(0);
			Cell cell = null;
			for (int i = 1; i < sheet.getRows(); i++) {
				PersonInfo tc = new PersonInfo();
				cell = sheet.getCell(0, i);
				tc.setName(cell.getContents());
				cell = sheet.getCell(1, i);
				tc.setPhone(cell.getContents());
				billList.add(tc);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return billList;
	}


}
