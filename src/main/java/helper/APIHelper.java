package helper;

import excel.report.dao.Suite;
import excel.report.dao.Test;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIHelper {

    private static final String OAUTH_PATH = Constants.OAUTH_PATH;
    private static final String USERNAME = Constants.USERNAME;
    private static final String PASSWORD = Constants.PASSWORD;
    private static final String BASIC_AUTH = Constants.BASIC_AUTH;
    private static final String TOKEN;

    static {
        RestAssured.baseURI = Constants.REPORT_PORTAL_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        TOKEN = getToken();
    }

    public static List<Suite> getSuites(Map<String, String> query) {
        return RestAssured.given()
            .basePath(Constants.PROJECT_PATH)
            .auth().oauth2(TOKEN)
            .queryParams(query)
            .get()
            .then().statusCode(200)
            .extract().body().jsonPath().getList("content.", Suite.class);
    }

    public static List<Test> getTests(Map<String, String> query) {
        return RestAssured.given()
            .basePath(Constants.PROJECT_PATH)
            .auth().oauth2(TOKEN)
            .queryParams(query)
            .get()
            .then().statusCode(200)
            .extract().body().jsonPath().getList("content.", Test.class);
    }

    private static String getToken() {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("grant_type", "password");
        formParams.put("username", USERNAME);
        formParams.put("password", PASSWORD);

        return RestAssured.given()
            .basePath(OAUTH_PATH)
            .contentType(ContentType.URLENC)
            .header("Authorization", BASIC_AUTH)
            .formParams(formParams)
            .post()
            .then().statusCode(200)
            .extract().body().jsonPath().getString("access_token");
    }
}
