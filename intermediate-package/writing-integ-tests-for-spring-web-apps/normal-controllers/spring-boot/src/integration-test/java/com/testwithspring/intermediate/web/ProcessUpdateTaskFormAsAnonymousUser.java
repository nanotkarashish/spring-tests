
package com.testwithspring.intermediate.web;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.testwithspring.intermediate.IntegrationTest;
import com.testwithspring.intermediate.IntegrationTestContext;
import com.testwithspring.intermediate.ReplacementDataSetLoader;
import com.testwithspring.intermediate.Tasks;
import com.testwithspring.intermediate.config.Profiles;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {IntegrationTestContext.class})
@AutoConfigureMockMvc
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
public class ProcessUpdateTaskFormAsAnonymousUser {

    private static final String NEW_DESCRIPTION = "The old lesson was not good";
    private static final String NEW_TITLE = "Rewrite an existing lesson";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnHttpStatusCodeFound() throws Exception {
        submitUpdateTaskForm()
                .andExpect(status().isFound());
    }

    @Test
    public void shouldRedirectUserToLoginPage() throws Exception {
        submitUpdateTaskForm()
                .andExpect(redirectedUrl(WebTestConstants.LOGIN_PAGE_URL));
    }

    @Test
    @ExpectedDatabase(value = "task.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void shouldNotUpdateTask() throws Exception {
        submitUpdateTaskForm();
    }

    private ResultActions submitUpdateTaskForm() throws Exception {
        return  mockMvc.perform(post("/task/{taskId}/update", Tasks.WriteLesson.ID)
                .param(WebTestConstants.ModelAttributeProperty.Task.DESCRIPTION, NEW_DESCRIPTION)
                .param(WebTestConstants.ModelAttributeProperty.Task.ID, Tasks.WriteLesson.ID.toString())
                .param(WebTestConstants.ModelAttributeProperty.Task.TITLE, NEW_TITLE)
                .with(csrf())
        );
    }
}
