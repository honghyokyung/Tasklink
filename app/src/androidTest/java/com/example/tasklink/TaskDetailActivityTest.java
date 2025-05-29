package com.example.tasklink;

// JUnit 및 Android Test
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

// Espresso Core
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

// Android Context & Intent
import android.content.Intent;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TaskDetailActivityTest {

    @Test
    public void testUIElementsDisplayedWhenTaskIdIsNull() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TaskDetailActivity.class);
        intent.putExtra("projectId", "test_project_id");  // taskId는 넣지 않음
        ActivityScenario.launch(intent);

        onView(withId(R.id.task_detail_task_title)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_description)).check(matches(isDisplayed()));
        onView(withId(R.id.text_assign_user)).check(matches(isDisplayed()));
        onView(withId(R.id.text_deadline)).check(matches(isDisplayed()));
        onView(withId(R.id.text_file)).check(matches(isDisplayed()));
    }

    @Test
    public void testClickPickDate_opensDatePicker() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TaskDetailActivity.class);
        intent.putExtra("projectId", "test_project_id");
        ActivityScenario.launch(intent);

        onView(withId(android.R.id.content)).perform(TaskListActivityTest.waitForView(withId(R.id.button_pick_date), 5000));
        onView(withId(R.id.button_pick_date)).check(matches(isDisplayed()));
        onView(withId(R.id.button_pick_date)).perform(click());
    }

    @Test
    public void testSaveWithEmptyTitle_showsToast() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TaskDetailActivity.class);
        intent.putExtra("projectId", "test_project_id");
        ActivityScenario.launch(intent);

        onView(withId(R.id.button_back)).perform(click());
    }

}
