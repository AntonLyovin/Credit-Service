package calculatorApp.calculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ScoringControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void calculateOfferTest() throws Exception {
        String requestBody = "{"
                + "\"amount\":200000,"
                + "\"term\":12,"
                + "\"firstName\":\"an\","
                + "\"lastName\":\"lez\","
                + "\"middleName\":\"dre\","
                + "\"gender\":\"MALE\","
                + "\"birthdate\":\"1995-06-17\","
                + "\"passportSeries\":\"0374\","
                + "\"passportNumber\":\"492684\","
                + "\"passportIssueDate\":\"2020-06-17\","
                + "\"passportIssueBranch\":\"string\","
                + "\"maritalStatus\":\"SINGLE\","
                + "\"dependentAmount\":0,"
                + "\"employment\":{"
                + "\"employmentStatus\":\"SELF_EMPLOYED\","
                + "\"employerINN\":\"string\","
                + "\"salary\":300000,"
                + "\"position\":\"DIRECTOR\","
                + "\"getWorkExperienceCurrent\":20,"
                + "\"workExperienceTotal\":20"
                + "},"
                + "\"accountNumber\":\"string\","
                + "\"isInsuranceEnabled\":true,"
                + "\"isSalaryClient\":true"
                + "}";

        mockMvc.perform(post("http://localhost:8080/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void calculateOfferValidationTest() throws Exception {
        String requestBody = "{"
                + "\"amount\":200000,"
                + "\"term\":12,"
                + "\"firstName\":\"an\","
                + "\"lastName\":\"lez\","
                + "\"middleName\":\"dre\","
                + "\"gender\":\"MALE\","
                + "\"birthdate\":\"1995-06-17\","
                + "\"passportSeries\":\"037\","
                + "\"passportNumber\":\"492684\","
                + "\"passportIssueDate\":\"2020-06-17\","
                + "\"passportIssueBranch\":\"string\","
                + "\"maritalStatus\":\"SINGLE\","
                + "\"dependentAmount\":0,"
                + "\"employment\":{"
                + "\"employmentStatus\":\"SELF_EMPLOYED\","
                + "\"employerINN\":\"string\","
                + "\"salary\":300000,"
                + "\"position\":\"DIRECTOR\","
                + "\"getWorkExperienceCurrent\":20,"
                + "\"workExperienceTotal\":20"
                + "},"
                + "\"accountNumber\":\"string\","
                + "\"isInsuranceEnabled\":true,"
                + "\"isSalaryClient\":true"
                + "}";

        mockMvc.perform(post("http://localhost:8080/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculatePreOfferTest() throws Exception {
        String requestBody = "{"
                + "\"amount\":200000,"
                + "\"term\":12,"
                + "\"firstName\":\"an\","
                + "\"lastName\":\"lez\","
                + "\"middleName\":\"dre\","
                + "\"email\":\"Ivanov@mail.ru\","
                + "\"birthdate\":\"1995-06-17\","
                + "\"passportSeries\":\"0374\","
                + "\"passportNumber\":\"492684\""
                + "}";
        mockMvc.perform(post("http://localhost:8080/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void calculatePreOfferValidationTest() throws Exception {
        String requestBody = "{"
                + "\"amount\":200000,"
                + "\"term\":12,"
                + "\"firstName\":\"an\","
                + "\"lastName\":\"lez\","
                + "\"middleName\":\"dre\","
                + "\"email\":\"Ivanov@mail.ru\","
                + "\"birthdate\":\"1995-06-17\","
                + "\"passportSeries\":\"074\","
                + "\"passportNumber\":\"492684\""
                + "}";
        mockMvc.perform(post("http://localhost:8080/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculatePreOfferWithInvalidDataTest() throws Exception {
        String invalidRequestBody = "{"
                + "\"amount\":1000,"
                + "\"term\":3,"
                + "\"firstName\":\"g\", "
                + "\"lastName\":\"d\", "
                + "\"middleName\":\"v\", "
                + "\"email\":\"invalid-email\","
                + "\"birthdate\":\"1990-12-01\","
                + "\"passportSeries\":\"12\","
                + "\"passportNumber\":\"12345\""
                + "}";

        mockMvc.perform(post("http://localhost:8080/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                // Проверка, что ошибка по 'amount'
                .andExpect(jsonPath("$.errors[?(@.field=='amount')].message").value("сумма должна быть не менее 20000"))
                // Проверка, что ошибка по 'term'
                .andExpect(jsonPath("$.errors[?(@.field=='term')].message").value("Срок должен не менее 6 месяцев"))
                // Проверка, что ошибка по 'firstName' содержит нужное сообщение
                .andExpect(jsonPath("$.errors[?(@.field=='firstName')].message", hasItem("Имя должно быть от 2 до 30 латинских букв")))
                // Аналогично для других полей...
                .andExpect(jsonPath("$.errors[?(@.field=='lastName')].message", hasItem("Фамилия должна быть от 2 до 30 латинских букв")))
                .andExpect(jsonPath("$.errors[?(@.field=='middleName')].message", hasItem("Отчество должно быть от 2 до 30 латинских букв")))
                .andExpect(jsonPath("$.errors[?(@.field=='email')].message", hasItem("Неправильный формат Email")))
                .andExpect(jsonPath("$.errors[?(@.field=='passportSeries')].message", hasItem("Серия паспорта должна состоять из 4 цифр")))
                .andExpect(jsonPath("$.errors[?(@.field=='passportNumber')].message", hasItem("Номер паспорта должен состоять из 6 цифр")));
    }
}