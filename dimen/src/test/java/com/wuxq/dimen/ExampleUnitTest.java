package com.wuxq.dimen;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {

        int[] items = new int[]{300, 310, 320, 330, 340, 350, 360, 370, 375, 380, 390, 400, 410, 420, 430, 440, 450, 460, 470, 480, 490, 520, 560, 600};

        int base = 375;
        int target = 600;
        float percent = (float) target / base;

        System.out.println(String.format("<dimen name=\"_0_5dp\">%sdp</dimen>",  String.format("%.2f", 0.5f * percent)));
        for (int i = 1; i <= 500; i++) {
            String value = String.format("%.2f", i * percent);
            System.out.println(String.format("<dimen name=\"_%sdp\">%sdp</dimen>", i, value));
        }

    }
}