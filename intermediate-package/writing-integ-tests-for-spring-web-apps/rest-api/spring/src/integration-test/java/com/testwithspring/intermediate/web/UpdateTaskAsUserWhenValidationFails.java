package com.testwithspring.intermediate.web;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.testwithspring.intermediate.*;
import com.testwithspring.intermediate.config.Profiles;
import com.testwithspring.intermediate.task.TaskFormDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IntegrationTestContext.class})
//@ContextConfiguration(locations = {"classpath:integration-test-context.xml"})
@WebAppConfiguration
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class,
        ServletTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class
})
@DatabaseSetup({
        "/com/testwithspring/intermediate/users.xml",
        "task.xml"
})
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class)
@Category(IntegrationTest.class)
@ActiveProfiles(Profiles.INTEGRATION_TEST)
public class UpdateTaskAsUserWhenValidationFails {

    @Autowired
    private WebApplicationContext webAppContext;

    private MockMvc mockMvc;

    @Before
    public void configureSystemUnderTest() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithUserDetails(Users.JohnDoe.EMAIL_ADDRESS)
    public void shouldReturnHttpStatusCodeBadRequest() throws Exception {
        updateTaskWithEmptyData()
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(Users.JohnDoe.EMAIL_ADDRESS)
    public void shouldReturnValidationErrorsAsJson() throws Exception {
        updateTaskWithEmptyData()
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    @WithUserDetails(Users.JohnDoe.EMAIL_ADDRESS)
    public void shouldReturnOneValidationError() throws Exception {
        updateTaskWithEmptyData()
                .andExpect(jsonPath("$.fieldErrors", hasSize(1)));
    }

    @Test
    @WithUserDetails(Users.JohnDoe.EMAIL_ADDRESS)
    public void shouldReturnValidationErrorAboutEmptyTitle() throws Exception {
        updateTaskWithEmptyData()
                .andExpect(jsonPath("$.fieldErrors[0].field", is("title")))
                .andExpect(jsonPath("$.fieldErrors[0].errorCode", is(WebTestConstants.ValidationErrorCode.EMPTY_FIELD)));
    }

    @Test
    @WithUserDetails(Users.JohnDoe.EMAIL_ADDRESS)
    @ExpectedDatabase(value = "task.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void shouldNotUpdateTask() throws Exception {
        updateTaskWithEmptyData();
    }

    private ResultActions updateTaskWithEmptyData() throws Exception {
        TaskFormDTO input = new TaskFormDTO();
        input.setDescription("");
        input.setId(Tasks.WriteLesson.ID);
        input.setTitle("");

        return mockMvc.perform(put("/api/task/{taskId}", Tasks.WriteLesson.ID)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(WebTestUtil.convertObjectToJsonBytes(input))
                .with(csrf())
        );
    }
}
