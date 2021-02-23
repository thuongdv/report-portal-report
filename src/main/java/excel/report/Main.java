package excel.report;

import helper.Constants;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.nio.file.Paths;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        val pwd = System.getProperty("user.dir");
        val templateFile = Paths.get(pwd, Constants.REPORT_TEMPLATE_FILE);
        val outputFile = Paths.get(pwd, Constants.REPORT_FILE);
        FileUtils.copyFile(templateFile.toFile(), outputFile.toFile());
        val rp2Excel = new RP2Excel();
        rp2Excel.generate(outputFile.toString());
    }
}
