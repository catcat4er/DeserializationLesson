import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.asynchttpclient.Request;
import pojo.CreateUserRequest;
import pojo.CreateUserResponse;
import pojo.UserPojo;
import org.junit.jupiter.api.Test;
import pojo.UserPojoFull;
import steps.UsersSteps;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class RestTest {

    private static final RequestSpecification REQ_SPEC = new RequestSpecBuilder()
            .setBaseUri("https://reqres.in/api")
            .setBasePath("/users")
            .setContentType(JSON)
            .build();

    @Test
    public void simpleTest() {
        given()
                .spec(REQ_SPEC)
                .when()
                .get()
                .then()
                .statusCode(200);
    }

    @Test
    //для сохранения в переменной
    public void simpleTestWithStringSaving() {
        String rs = given()
                .spec(REQ_SPEC)
                .when().get()
                .then().statusCode(200)
                .extract().asString();
    }

    @Test
    //Применяем синтаксис лямбд find{it}
    public void simpleTestWithFind() {
        given()
                .spec(REQ_SPEC)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("data.find{it.email=='george.bluth@reqres.in'}.first_name",
                        equalTo("George"));
    }

    @Test
    //Сохранение в переменную весь массив data.email
    public void simpleTestWithList() {
        List<String> emails =  given()
                .spec(REQ_SPEC)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("data.email");
    }

    @Test
    //Применяем lombok для десериализации
    public void simpleTestWithPojo() {
        List<UserPojoFull> users =  given()
                .spec(REQ_SPEC)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("data", UserPojoFull.class);
    }
    @Test
    //Добавляем assertj для проверки с использованием лямбды
    public void simplePojoTestWithAssertJ() {
        List<UserPojoFull> users =  given()
                .spec(REQ_SPEC)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("data", UserPojoFull.class);
        assertThat(users).extracting(UserPojoFull::getEmail).contains("george.bluth@reqres.in");
    }

    @Test
    //Применение парсера даты
    public void simpleTestCreateUser() {

        CreateUserRequest rq = new CreateUserRequest();
        rq.setName("Bob");
        rq.setJob("Automation");

        CreateUserResponse rs = given()
                .spec(REQ_SPEC)
                .body(rq)
                .when().post()
                .then().extract().as(CreateUserResponse.class);

        assertThat(rs)
                .isNotNull()
                .extracting(CreateUserResponse::getName)
                .isEqualTo(rq.getName());

    }
    @Test
    //Подключение Степ-классов
    public void simpleTestWithSteps() {
        List<UserPojoFull> users = UsersSteps.getUsers();
        assertThat(users).extracting(UserPojoFull::getEmail).contains("george.bluth@reqres.in");
    }
}
