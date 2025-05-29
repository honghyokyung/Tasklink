package com.example.tasklink;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TaskListActivityTest {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testTaskListActivityLoadsRecyclerView() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TaskListActivity.class);
        intent.putExtra("projectTitle", "Test Project");
        intent.putExtra("projectId", "test_project_id");
        ActivityScenario.launch(intent);

        onView(withId(android.R.id.content)).perform(waitForView(withId(R.id.rvTasks), 5000));

        onView(withId(R.id.rvTasks)).check(matches(isDisplayed()));
    }

    @Test
    public void testAddTaskButtonNavigatesToAddTaskActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TaskListActivity.class);
        intent.putExtra("projectTitle", "Test Project");
        intent.putExtra("projectId", "test_project_id");
        ActivityScenario.launch(intent);

        onView(withId(android.R.id.content)).perform(waitForView(withId(R.id.btn_add_task), 5000));
        onView(withId(R.id.btn_add_task)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_add_task)).perform(click());

        intended(hasComponent(TaskSettingActivity.class.getName()));  // 🔄 이 부분은 실제 이동되는 Activity로 바꿔주세요
    }

    public static ViewAction waitForView(final Matcher<View> viewMatcher, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return withId(android.R.id.content);
            }

            @Override
            public String getDescription() {
                return "wait for a specific view with matcher <" + viewMatcher + "> during " + millis + " millis.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        if (viewMatcher.matches(child)) return;
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);

                throw new AssertionError("View not found: " + HumanReadables.describe(view));
            }
        };
    }
}
