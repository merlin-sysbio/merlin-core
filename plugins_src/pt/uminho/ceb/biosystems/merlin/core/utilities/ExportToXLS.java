package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.io.FileOutputStream;

import javax.swing.table.TableColumnModel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;

public class ExportToXLS {
	
	public static void exportToXLS(String excelFileName, DataTable mainTableData, MyJTable jTable){
		
		try {
			String sheetName = "Sheet1";//name of sheet

			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet(sheetName) ;

			HSSFRow row = sheet.createRow(0);
			
			TableColumnModel tc = jTable.getColumnModel();

			int i = 0;
			
			while (i < tc.getColumnCount()) {
				
				row.createCell(i).setCellValue(tc.getColumn(i).getHeaderValue().toString());
				i++;
			}
			
			for (int r=0;r < mainTableData.getRowCount(); r++ )
			{
				row = sheet.createRow(r+2);

				//iterating c number of columns
				for (int c=0;c < mainTableData.getColumnCount(); c++ )
				{
					HSSFCell cell = row.createCell(c);
					
					if(mainTableData.getValueAt(r, c)!=null)
						cell.setCellValue(mainTableData.getValueAt(r, c).toString());
				}
			}
			
			FileOutputStream fileOut = new FileOutputStream(excelFileName);
			
			//write this workbook to an Outputstream.
			wb.write(fileOut);
			fileOut.flush();
			wb.close();
			fileOut.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
