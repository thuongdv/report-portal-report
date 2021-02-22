package helper;

import enums.DefinedCellStyle;
import lombok.Getter;
import lombok.SneakyThrows;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExcelHelper {
    private final Workbook wb;
    @Getter
    private final String filePath;

    @SneakyThrows
    public ExcelHelper(String filePath, boolean readOnly) {
        this.filePath = filePath;
        File excelFile = new File(filePath);
        if (!excelFile.exists()) {
            // in read only mode, just read, don't create
            if (readOnly) throw new IOException(String.format("File not found: %s", filePath));
            wb = new XSSFWorkbook();
        } else {
            FileInputStream fis = new FileInputStream(filePath);
            //by pass large file exception from POI
            ZipSecureFile.setMinInflateRatio(0);
            wb = new XSSFWorkbook(fis);
            fis.close();
        }
    }

    public ExcelHelper(String filePath) {
        this(filePath, false);
    }

    private void cleanUp() throws IOException {
        wb.close();
    }

    public Sheet getSheet(int sheetIdx) {
        return wb.getSheetAt(sheetIdx);
    }

    public String getSheetName(int sheetIdx) {
        return wb.getSheetAt(sheetIdx).getSheetName();
    }

    private int getSheetIndex(String sheetName) {
        return wb.getSheetIndex(sheetName);
    }

    private Sheet getSheet(String sheetName) {
        return wb.getSheet(sheetName);
    }

    public int getNumberOfSheets() {
        return wb.getNumberOfSheets();
    }

    public boolean doesSheetExist(String sheetName) {
        return wb.getSheetIndex(sheetName) >= 0;
    }

    public void createSheet(String sheetName) {
        wb.createSheet(sheetName);
    }

    public void createSheet(String sheetName, int at) {
        wb.createSheet(sheetName);
        wb.setSheetOrder(sheetName, at);
    }

    public void createRows(String sheetName, int startingRowIdx, Map<Integer, List<String>> data) {
        for (Map.Entry<Integer, List<String>> entry : data.entrySet()) {
            createRow(sheetName, startingRowIdx, entry.getValue());
            startingRowIdx++;
        }
    }

    public void createRowsWithFullBorder(String sheetName, int startingRowIdx, Map<Integer, List<String>> data) {
        for (Map.Entry<Integer, List<String>> entry : data.entrySet()) {
            createRow(sheetName, startingRowIdx, entry.getValue());
            formatRow(sheetName, startingRowIdx, Arrays.asList(DefinedCellStyle.BORDER_ALL, DefinedCellStyle.WRAP_TEXT));
            startingRowIdx++;
        }
    }

    public void createRow(String sheetName, int rowIdx, List<String> rowData) {
        this.createRow(sheetName, rowIdx, 0, rowData);
    }

    public void createRow(String sheetName, int rowIdx, int startingColIdx, List<String> rowData) {
        Sheet sheet = getSheet(sheetName);
        Row newRow = sheet.createRow(rowIdx);
        int cellIndex = startingColIdx;
        for (String cellValue : rowData) {
            Cell newCell = newRow.createCell(cellIndex);
            newCell.setCellValue(cellValue);
            cellIndex++;
        }
    }

    public void createRowWithFormat(String sheetName, int rowIdx, int startingColIdx, List<String> rowData,
                                    List<DefinedCellStyle> cellStyles) {
        Sheet sheet = getSheet(sheetName);
        Row row = sheet.createRow(rowIdx);
        int colIdx = startingColIdx;
        for (String cellValue : rowData) {
            Cell cell = row.createCell(colIdx);
            if (StringUtils.isNumeric(cellValue)) cell.setCellValue(Double.parseDouble(cellValue));
            else cell.setCellValue(cellValue);
            setCellStyle(cell, cellStyles);
            colIdx++;
        }
    }

    public void formatRow(String sheetName, int rowIdx, List<DefinedCellStyle> cellStyles) {
        Sheet sheet = getSheet(sheetName);
        Row row = sheet.getRow(rowIdx);
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            setCellStyle(cell, cellStyles);
        }
    }

    public void autoSizeColumn(String sheetName, int rowIdx) {
        int numberOfColumn = getNumberOfColumns(sheetName, rowIdx);
        for (int i = 0; i < numberOfColumn; i++)
            getSheet(sheetName).autoSizeColumn(i);
    }

    public void setColumnWidth(String sheetName, int fromColumnIdx, List<Integer> widthList) {
        for (int width : widthList) {
            // For more details about what the 256 is, please see the setColumnWidth method doc
            getSheet(sheetName).setColumnWidth(fromColumnIdx, width * 256);
            ++fromColumnIdx;
        }
    }

    public int getNumberOfRows(int sheetIdx) {
        return getNumberOfRows(getSheet(sheetIdx));
    }

    public int getNumberOfRows(String sheetName) {
        return getNumberOfRows(getSheet(sheetName));
    }

    private int getNumberOfRows(Sheet sheet) {
        int i = 0;
        while (i <= sheet.getLastRowNum()) {
            Row row = sheet.getRow(i);
            if (row == null) break;
            Iterator<Cell> cellIterator = row.cellIterator();
            Cell currentCell = null;
            //Row is empty when all of cells in it are empty
            while (cellIterator.hasNext()) {
                currentCell = cellIterator.next();
                if (currentCell != null && !currentCell.toString().equals("")) {
                    break;
                }
            }
            if (currentCell == null || currentCell.toString().equals(""))
                break;
            i++;
        }
        return i;
    }

    private void writeToFile(String fileName) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileName);
        wb.write(fileOut);
        fileOut.close();
    }

    public int getNumberOfColumns(String sheetName, int rowIdx) {
        Sheet sheet = getSheet(sheetName);
        return sheet.getRow(rowIdx).getPhysicalNumberOfCells();
    }

    public void saveAndClose() throws IOException {
        assert wb != null;
        writeToFile(this.filePath);
        wb.close();
    }

    public CellStyle createCellStyle(CellStyle cellStyle, DefinedCellStyle styleName) {
        switch (styleName) {
            case WRAP_TEXT:
                cellStyle.setWrapText(true);
                break;
            case ALIGN_CENTER:
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                break;
            case SET_COLOR_LIGHT_GREEN:
                cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case SET_COLOR_LIGHT_BLUE:
                cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case BORDER_ALL:
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
            case FORMAT_AS_TEXT:
                DataFormat fmt = wb.createDataFormat();
                cellStyle.setDataFormat(fmt.getFormat("@"));
                break;
            case TOP_ALIGN:
                cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        }
        return cellStyle;
    }

    public void setCellStyle(Cell cell, DefinedCellStyle style) {
        CellStyle cellStyle = wb.createCellStyle();
        cell.setCellStyle(createCellStyle(cellStyle, style));
    }

    public void setCellStyle(Cell cell, List<DefinedCellStyle> styles) {
        CellStyle cellStyle = wb.createCellStyle();
        for (DefinedCellStyle style : styles) {
            createCellStyle(cellStyle, style);
        }
        cell.setCellStyle(cellStyle);
    }
}
