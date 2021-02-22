package excel.report;

import helper.Constants;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Paths;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        val pwd = System.getProperty("user.dir");
        val file = Paths.get(pwd, Constants.REPORT_TEMPLATE_FILE);
        val rp2Excel = new RP2Excel();
        rp2Excel.generate(file.toString());
    }
}
