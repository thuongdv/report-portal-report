package helper;

import lombok.val;

public class Constants {
    // Date time format
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";

    public static final String LAUNCH_ID = PropertiesHelper.getPropValue("launch.id");
    public static final String PROJECT_PATH = PropertiesHelper.getPropValue("project.path");
    public static final String REPORT_PORTAL_URL = PropertiesHelper.getPropValue("report.portal.url");
    public static final String OAUTH_PATH = PropertiesHelper.getPropValue("oauth.path");
    public static final String USERNAME = PropertiesHelper.getPropValue("credentials.username");
    public static final String PASSWORD = PropertiesHelper.getPropValue("credentials.password");
    public static final String BASIC_AUTH = PropertiesHelper.getPropValue("basic.auth");

    public static final String REPORT_TEMPLATE_FILE = PropertiesHelper.getPropValue("report.template.file");
}
