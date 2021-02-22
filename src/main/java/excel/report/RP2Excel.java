package excel.report;

import enums.DefinedCellStyle;
import excel.report.dao.Suite;
import excel.report.dao.Test;
import helper.APIHelper;
import helper.Constants;
import helper.DateTimeHelper;
import helper.ExcelHelper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RP2Excel {

    private String sheetName;
    private ExcelHelper excelHelper;
    private static final int NEW_SHEET_POSITION = 1;
    private static final int HEADER_ROW_IDX = 0;
    private static final int START_ROW_IDX = 1;
    private static final int START_COL_IDX = 1;

    @SneakyThrows
    public void generate(String toFile) {
        this.excelHelper = new ExcelHelper(toFile);
        this.sheetName = DateTimeHelper.getToday("yyyy-MM-dd_HH-mm-ss");

        this.createNewSheet();
        this.writeHeader();
        this.writeData();
        this.formatData();
        this.finish();
    }

    @SneakyThrows
    private void createNewSheet() {
        this.excelHelper.createSheet(this.sheetName, NEW_SHEET_POSITION);
    }

    private void writeHeader() {
        this.excelHelper.createRow(this.sheetName, HEADER_ROW_IDX, START_COL_IDX, RowData.Header.headers());
        this.excelHelper.formatRow(this.sheetName, HEADER_ROW_IDX, Arrays.asList(
            DefinedCellStyle.SET_COLOR_LIGHT_GREEN, DefinedCellStyle.ALIGN_CENTER,
            DefinedCellStyle.WRAP_TEXT, DefinedCellStyle.BORDER_ALL
        ));

        // Set column width
        this.excelHelper.setColumnWidth(this.sheetName, START_COL_IDX, RowData.Header.widths());
    }

    @SneakyThrows
    private void writeData() {
        int currentRowIdx = START_ROW_IDX;
        List<Suite> suites = this.getSuites();
        for (Suite suite : suites) {
            List<Test> tests = this.getTests(suite.getPath());
            String comment = String.join("\n", this.getComments(tests));
            this.excelHelper.createRowWithFormat(
                this.sheetName, currentRowIdx, START_COL_IDX, new RowData(suite, comment).toList(),
                Arrays.asList(DefinedCellStyle.WRAP_TEXT, DefinedCellStyle.TOP_ALIGN, DefinedCellStyle.BORDER_ALL)
            );
            currentRowIdx++;
        }
    }

    private void formatData() {
        SheetConditionalFormatting sheetCF = this.excelHelper.getSheet(NEW_SHEET_POSITION).getSheetConditionalFormatting();

        // Format FAILED data
        ConditionalFormattingRule ruleFailed = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"FAILED\"");
        PatternFormatting fillFailed = ruleFailed.createPatternFormatting();
        fillFailed.setFillBackgroundColor(IndexedColors.RED.index);
        fillFailed.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        // Format PASSED data
        ConditionalFormattingRule rulePassed = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"PASSED\"");
        PatternFormatting fillPassed = rulePassed.createPatternFormatting();
        fillPassed.setFillBackgroundColor(IndexedColors.GREEN.index);
        fillPassed.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        ConditionalFormattingRule[] cfRules = new ConditionalFormattingRule[]{ruleFailed, rulePassed};

        CellRangeAddress[] regions = new CellRangeAddress[]{CellRangeAddress.valueOf("C2:C120")};

        sheetCF.addConditionalFormatting(regions, cfRules);
    }

    private List<Suite> getSuites() {
        // Call Report Portal's APIs then write data to Data objects
        // filter.eq.launchId=875&filter.level.path=1&page.page=1&page.size=70&page.sort=startTime,ASC
        Map<String, String> query = new HashMap<>();
        query.put("filter.eq.launchId", Constants.LAUNCH_ID);
        query.put("filter.level.path", "1");
        query.put("page.page", "1");
        query.put("page.size", "70");
        query.put("page.sort", "startTime,ASC");
        return APIHelper.getSuites(query)
            .stream()
            .sorted((a, b) -> a.getStatus().compareToIgnoreCase(b.getStatus()))
            .collect(Collectors.toList());
    }

    private List<Test> getTests(String parentId) {
        // Call Report Portal's APIs then write data to Data objects
        // filter.eq.launchId=875&filter.eq.parentId=161810&page.page=1&page.size=50&page.sort=startTime,ASC
        Map<String, String> query = new HashMap<>();
        query.put("filter.eq.launchId", Constants.LAUNCH_ID);
        query.put("filter.eq.parentId", String.valueOf(parentId));
        query.put("filter.in.status", "FAILED,INTERRUPTED");
        query.put("page.page", "1");
        query.put("page.size", "70");
        query.put("page.sort", "startTime,ASC");
        return APIHelper.getTests(query);
    }

    private List<String> getComments(List<Test> tests) {
        return tests.stream()
            .map(e -> e.getIssue().getComment())
            .distinct()
            .collect(Collectors.toList());
    }

    @SneakyThrows
    private void finish() {
        this.excelHelper.saveAndClose();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    private static class RowData {
        @AllArgsConstructor
        enum Header {
            FEATURE("Feature", 60),
            STATUS("Status", 15),
            TOTAL("Total", 10),
            PASSED("Passed", 10),
            SKIPPED("Failed", 10),
            FAILED("Skipped", 10),
            BUG("Bug", 30);

            String title;
            Integer width;

            static List<String> headers() {
                return Arrays.stream(Header.values()).map(h -> h.title).collect(Collectors.toList());
            }

            static List<Integer> widths() {
                return Arrays.stream(Header.values()).map(h -> h.width).collect(Collectors.toList());
            }
        }

        String feature;
        String status;
        int total;
        int passed;
        int skipped;
        int failed;
        String bug;

        public RowData(Suite suite, String comment) {
            this.feature = suite.getName();
            this.status = suite.getStatus();
            this.total = suite.getStatistics().getExecutions().getTotal();
            this.passed = suite.getStatistics().getExecutions().getPassed();
            this.failed = suite.getStatistics().getExecutions().getFailed();
            this.skipped = suite.getStatistics().getExecutions().getSkipped();
            this.bug = comment;
        }

        public List<String> toList() {

            return Arrays.asList(this.feature, status,
                String.valueOf(this.total),
                String.valueOf(this.passed),
                String.valueOf(this.failed),
                String.valueOf(this.skipped),
                this.bug
            );
        }
    }
}
