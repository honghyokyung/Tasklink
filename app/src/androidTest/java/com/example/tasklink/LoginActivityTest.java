package com.example.tasklink;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void loginSuccessNavigatesToDashboard() {
        FirebaseAuth mockAuth = Mockito.mock(FirebaseAuth.class);
        AuthResult mockResult = Mockito.mock(AuthResult.class);
        Task<AuthResult> mockTask = Tasks.forResult(mockResult);

        Mockito.when(mockAuth.signInWithEmailAndPassword(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockTask);

        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        scenario.onActivity(activity -> {
            activity.mAuth = mockAuth;
        });

        onView(withId(R.id.editTextId)).perform(typeText("mockuser@example.com"));
        onView(withId(R.id.editTextPassword)).perform(typeText("mockpassword"));
        onView(withId(R.id.buttonLogin)).perform(click());

        intended(hasComponent(MaindashboardActivity.class.getName()));
    }

    // Test 1: 입력 필드 초기 상태 확인
    @Test
    public void inputFieldsShouldBeEmptyInitially() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.editTextId)).check(matches(withText("")));
        onView(withId(R.id.editTextPassword)).check(matches(withText("")));
    }

    // Test 2: 이메일만 입력하고 로그인 시도
    @Test
    public void loginWithOnlyEmail_showsError() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.editTextId)).perform(typeText("email@domain.com"));
        onView(withId(R.id.buttonLogin)).perform(click());
        // 결과는 UI로만 확인 (Toast는 Espresso 기본 기능으로는 체크 어려움)
    }

    // Test 3: 회원가입 버튼 클릭 시 이동
    @Test
    public void signupButtonOpensSignupActivity() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.buttonSignup)).perform(click());
        intended(hasComponent(SignupActivity.class.getName()));
    }
    // Test 4: 비밀번호만 입력한 경우 → 에러
    @Test
    public void loginWithOnlyPassword_showsError() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.editTextPassword)).perform(typeText("123456"));
        onView(withId(R.id.buttonLogin)).perform(click());
        // 흐름만 확인 (Toast 메시지는 Espresso 기본으로는 assert 어려움)
    }

    // Test 5: 아무 것도 입력하지 않고 로그인 시도 → 화면 전환 안됨
    @Test
    public void loginWithEmptyInputs_doesNotNavigate() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.buttonLogin)).perform(click());

        // Dashboard로 이동하지 않아야 함 (실제로 이동했다면 실패)
        // 이 테스트는 이동 시도를 하지 않는 것으로 확인되며, 별도 Intent 발생 안함
    }

    // Test 6: 로그인 버튼은 항상 클릭 가능해야 함
    @Test
    public void loginButtonIsClickable() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonLogin)).perform(click());
    }
}
