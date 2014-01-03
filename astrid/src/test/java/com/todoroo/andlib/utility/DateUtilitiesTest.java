/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.andlib.utility;

import android.content.Context;

import com.todoroo.andlib.test.TodorooRobolectricTestCase;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.tasks.Snippet;

import java.util.Date;

import static com.todoroo.andlib.utility.DateUtilities.clearTime;
import static com.todoroo.andlib.utility.DateUtilities.getRelativeDay;
import static com.todoroo.andlib.utility.DateUtilities.getStartOfDay;
import static com.todoroo.andlib.utility.DateUtilities.isEndOfMonth;
import static com.todoroo.andlib.utility.DateUtilities.oneMonthFromNow;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.getShadowApplication;
import static org.tasks.Freeze.freezeAt;
import static org.tasks.Freeze.freezeClock;
import static org.tasks.date.DateTimeUtils.currentTimeMillis;
import static org.tasks.date.DateTimeUtils.newDate;

@RunWith(RobolectricTestRunner.class)
public class DateUtilitiesTest extends TodorooRobolectricTestCase {

    public void set24Hour(boolean is24) {
        DateUtilities.is24HourOverride = is24;
    }

    @Test
    public void testTimeString() {
        forEachLocale(new Runnable() {
            public void run() {
                Date d = newDate();

                set24Hour(false);
                for (int i = 0; i < 24; i++) {
                    d.setHours(i);
                    DateUtilities.getTimeString(getContext(), d);
                }

                set24Hour(true);
                for (int i = 0; i < 24; i++) {
                    d.setHours(i);
                    DateUtilities.getTimeString(getContext(), d);
                }
            }
        });
    }

    @Test
    public void testDateString() {
        forEachLocale(new Runnable() {
            public void run() {
                Date d = newDate();

                for (int i = 0; i < 12; i++) {
                    d.setMonth(i);
                    DateUtilities.getDateString(d);
                }
            }
        });
    }

    @Test
    public void oneMonthFromStartOfDecember() {
        DateTime now = new DateTime(2013, 12, 1, 12, 19, 45, 192);
        final long expected = new DateTime(2014, 1, 1, 12, 19, 45, 192).getMillis();

        freezeAt(now).thawAfter(new Snippet() {{
            assertEquals(expected, oneMonthFromNow());
        }});
    }

    @Test
    public void oneMonthFromEndOfDecember() {
        DateTime now = new DateTime(2013, 12, 31, 16, 31, 20, 597);
        final long expected = new DateTime(2014, 1, 31, 16, 31, 20, 597).getMillis();

        freezeAt(now).thawAfter(new Snippet() {{
            assertEquals(expected, oneMonthFromNow());
        }});
    }

    @Test
    public void oneMonthFromEndOfJanuary() {
        DateTime now = new DateTime(2014, 1, 31, 12, 54, 33, 175);
        final long expected = new DateTime(2014, 3, 3, 12, 54, 33, 175).getMillis();

        freezeAt(now).thawAfter(new Snippet() {{
            assertEquals(expected, oneMonthFromNow());
        }});
    }

    @Test
    public void oneMonthFromEndOfFebruary() {
        DateTime now = new DateTime(2014, 2, 28, 9, 19, 7, 990);
        final long expected = new DateTime(2014, 3, 28, 9, 19, 7, 990).getMillis();

        freezeAt(now).thawAfter(new Snippet() {{
            assertEquals(expected, oneMonthFromNow());
        }});
    }

    @Test
    public void clearTimeFromDate() {
        DateTime now = new DateTime(2014, 1, 3, 10, 34, 32, 98);
        assertEquals(
                now.withMillisOfDay(0).getMillis(),
                clearTime(new Date(now.getMillis())));
    }

    @Test
    public void shouldGetStartOfDay() {
        DateTime now = new DateTime(2014, 1, 3, 10, 41, 41, 520);
        assertEquals(
                now.withMillisOfDay(0).getMillis(),
                getStartOfDay(now.getMillis()));
    }

    @Test
    public void checkEndOfMonth() {
        assertTrue(isEndOfMonth(newDate(2014, 1, 31)));
        assertTrue(isEndOfMonth(newDate(2014, 2, 28)));
        assertTrue(isEndOfMonth(newDate(2014, 3, 31)));
        assertTrue(isEndOfMonth(newDate(2014, 4, 30)));
        assertTrue(isEndOfMonth(newDate(2014, 5, 31)));
        assertTrue(isEndOfMonth(newDate(2014, 6, 30)));
        assertTrue(isEndOfMonth(newDate(2014, 7, 31)));
        assertTrue(isEndOfMonth(newDate(2014, 8, 31)));
        assertTrue(isEndOfMonth(newDate(2014, 9, 30)));
        assertTrue(isEndOfMonth(newDate(2014, 10, 31)));
        assertTrue(isEndOfMonth(newDate(2014, 11, 30)));
        assertTrue(isEndOfMonth(newDate(2014, 12, 31)));
    }
    
    @Test
    public void notTheEndOfTheMonth() {
        for(int month = 1 ; month <= 12 ; month++) {
            int lastDay = new DateTime(2014, month, 1, 0, 0, 0, 0).dayOfMonth().getMaximumValue();
            for(int day = 1 ; day < lastDay ; day++) {
                assertFalse(isEndOfMonth(newDate(2014, month, day)));
            }
        }
    }

    @Test
    public void checkEndOfMonthDuringLeapYear() {
        assertFalse(isEndOfMonth(newDate(2016, 2, 28)));
        assertTrue(isEndOfMonth(newDate(2016, 2, 29)));
    }

    @Test
    public void relativeDayIsToday() {
        final Context context = getShadowApplication().getApplicationContext();
        freezeClock().thawAfter(new Snippet() {{
            final long today = currentTimeMillis();
            assertEquals("today", getRelativeDay(context, today));
            assertEquals("today", getRelativeDay(context, today, false));
        }});
    }

    @Test
    public void relativeDayIsTomorrow() {
        final Context context = getShadowApplication().getApplicationContext();
        freezeClock().thawAfter(new Snippet() {{
            final long tomorrow = now().plusDays(1).getMillis();
            assertEquals("tmrw", getRelativeDay(context, tomorrow));
            assertEquals("tomorrow", getRelativeDay(context, tomorrow, false));
        }});
    }

    @Test
    public void relativeDayIsYesterday() {
        final Context context = getShadowApplication().getApplicationContext();
        freezeClock().thawAfter(new Snippet() {{
            final long yesterday = now().minusDays(1).getMillis();
            assertEquals("yest", getRelativeDay(context, yesterday));
            assertEquals("yesterday", getRelativeDay(context, yesterday, false));
        }});
    }
}