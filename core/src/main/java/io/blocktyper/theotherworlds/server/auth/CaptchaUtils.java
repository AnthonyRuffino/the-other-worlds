package io.blocktyper.theotherworlds.server.auth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.blocktyper.theotherworlds.server.messaging.Drawable;
import io.blocktyper.theotherworlds.server.messaging.Line;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CaptchaUtils {

    private final static int CHALLENGE_LENGTH = 5;

    private static int BASE_UNIT = 30;
    private static float THICKNESS = BASE_UNIT / 5;
    private static float WIGGLE = THICKNESS;
    private static int BASE_UNIT_2 = BASE_UNIT * 2;
    private static int BASE_UNIT_3 = BASE_UNIT * 3;
    private static int BASE_UNIT_4 = BASE_UNIT * 4;

    private static Vector2 START = new Vector2(BASE_UNIT * 2, BASE_UNIT * 2);
    private static Vector2 NO_SHIFT = new Vector2(0, 0);

    private static Vector2 UP_1 = new Vector2(0, BASE_UNIT);
    private static Vector2 DOWN_1 = new Vector2(0, -BASE_UNIT);
    private static Vector2 LEFT_1 = new Vector2(-BASE_UNIT, 0);
    private static Vector2 RIGHT_1 = new Vector2(BASE_UNIT, 0);

    private static Vector2 UP_1_RIGHT_1 = new Vector2(BASE_UNIT, BASE_UNIT);
    private static Vector2 DOWN_1_RIGHT_1 = new Vector2(BASE_UNIT, -BASE_UNIT);
    private static Vector2 UP_1_LEFT_1 = new Vector2(-BASE_UNIT, BASE_UNIT);
    private static Vector2 DOWN_1_LEFT_1 = new Vector2(-BASE_UNIT, -BASE_UNIT);

    private static Vector2 UP_2 = new Vector2(0, BASE_UNIT_2);
    private static Vector2 DOWN_2 = new Vector2(0, -BASE_UNIT_2);
    private static Vector2 LEFT_2 = new Vector2(-BASE_UNIT_2, 0);
    private static Vector2 RIGHT_2 = new Vector2(BASE_UNIT_2, 0);

    private static Vector2 UP_2_RIGHT_2 = new Vector2(BASE_UNIT_2, BASE_UNIT_2);

    private static Vector2 RIGHT_3 = new Vector2(BASE_UNIT_3, 0);
    private static Vector2 RIGHT_4 = new Vector2(BASE_UNIT_4, 0);

    private static Vector2 DOWN_3_LEFT_3 = new Vector2(-BASE_UNIT_3, -BASE_UNIT_3);

    private static Vector2 UP_1_RIGHT_2 = new Vector2(BASE_UNIT * 2, BASE_UNIT);

    private static final List<Map.Entry<Character, List<Map.Entry<Vector2, Vector2>>>> ALPHABET = new ArrayList<>();

    static {
        ALPHABET.add(new AbstractMap.SimpleEntry<>('1', List.of(
                new AbstractMap.SimpleEntry<>(RIGHT_2, RIGHT_2),
                new AbstractMap.SimpleEntry<>(LEFT_1, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT * 3, -BASE_UNIT * 6), NO_SHIFT)
        )));

        ALPHABET.add(new AbstractMap.SimpleEntry<>('2', List.of(
                new AbstractMap.SimpleEntry<>(RIGHT_2, RIGHT_2),
                new AbstractMap.SimpleEntry<>(LEFT_2, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT * 3, -BASE_UNIT * 6), NO_SHIFT)
        )));

        ALPHABET.add(new AbstractMap.SimpleEntry<>('3', List.of(
                new AbstractMap.SimpleEntry<>(RIGHT_2, RIGHT_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(RIGHT_2, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT * 3, -BASE_UNIT * 7), NO_SHIFT)
        )));

        ALPHABET.add(new AbstractMap.SimpleEntry<>('4', List.of(
                new AbstractMap.SimpleEntry<>(UP_2_RIGHT_2, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2_RIGHT_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_1),
                new AbstractMap.SimpleEntry<>(LEFT_1, LEFT_2),
                new AbstractMap.SimpleEntry<>(RIGHT_2, DOWN_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1),
                new AbstractMap.SimpleEntry<>(RIGHT_2, NO_SHIFT)
        )));

        ALPHABET.add(new AbstractMap.SimpleEntry<>('5', List.of(
                new AbstractMap.SimpleEntry<>(UP_2_RIGHT_2, DOWN_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_1),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT, -BASE_UNIT * 7), NO_SHIFT)

        )));

        ALPHABET.add(new AbstractMap.SimpleEntry<>('6', List.of(
                new AbstractMap.SimpleEntry<>(UP_1_RIGHT_2, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(DOWN_3_LEFT_3, UP_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT * 4, -BASE_UNIT), NO_SHIFT)

        )));

        ALPHABET.add(new AbstractMap.SimpleEntry<>('7', List.of(
                new AbstractMap.SimpleEntry<>(RIGHT_3, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(RIGHT_1, RIGHT_1),
                new AbstractMap.SimpleEntry<>(LEFT_1, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT * 5, -BASE_UNIT * 7), NO_SHIFT)

        )));


        ALPHABET.add(new AbstractMap.SimpleEntry<>('8', List.of(
                new AbstractMap.SimpleEntry<>(UP_1_RIGHT_2, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(RIGHT_1, DOWN_1_RIGHT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1_LEFT_1),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT * 4, -BASE_UNIT), NO_SHIFT)

        )));


        ALPHABET.add(new AbstractMap.SimpleEntry<>('9', List.of(
                new AbstractMap.SimpleEntry<>(RIGHT_4, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, UP_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, LEFT_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_2),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, DOWN_1),
                new AbstractMap.SimpleEntry<>(NO_SHIFT, RIGHT_2),
                new AbstractMap.SimpleEntry<>(new Vector2(BASE_UNIT * 2, -BASE_UNIT * 4), NO_SHIFT)

        )));

    }

    private static int ALPHABET_LENGTH = ALPHABET.size();


    public static Map.Entry<String, List<? extends Drawable>> getCaptcha() {


        List<Map.Entry<Character, List<Map.Entry<Vector2, Vector2>>>> vectors = IntStream.range(0, CHALLENGE_LENGTH).mapToObj(i ->
                ALPHABET.get(Math.toIntExact(Math.round((ALPHABET_LENGTH - 1) * Math.random())))
        ).collect(Collectors.toList());

        StringBuilder captchaBuilder = new StringBuilder();
        vectors.forEach(entry -> captchaBuilder.append(entry.getKey()));

        String captcha = captchaBuilder.toString();

        List<Line> drawables = new ArrayList<>();
        for (int i = 0; i < vectors.size(); i++) {
            Vector2 startPoint;
            if (i == 0) {
                startPoint = START;
            } else {
                Line previousLine = drawables.get(drawables.size() - 1);
                startPoint = new Vector2(previousLine.x2, previousLine.y2);
            }
            List<Map.Entry<Vector2, Vector2>> lines = vectors.get(i).getValue();

            drawables.addAll(getCaptcha(lines, startPoint));
        }

        return new AbstractMap.SimpleEntry<>(captcha, drawables);
    }

    private static List<Line> getCaptcha(List<Map.Entry<Vector2, Vector2>> pointPairs, Vector2 startingPoint) {
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < pointPairs.size(); i++) {
            Vector2 start;
            if (i == 0) {
                start = startingPoint;
            } else {
                Line previousLine = lines.get(i - 1);
                start = new Vector2(previousLine.x2, previousLine.y2);
            }
            Map.Entry<Vector2, Vector2> pointPair = pointPairs.get(i);
            float x1 = start.x + pointPair.getKey().x + wiggle();
            float y1 = start.y + pointPair.getKey().y + wiggle();
            float x2 = x1 + pointPair.getValue().x + wiggle();
            float y2 = y1 + pointPair.getValue().y + wiggle();
            String uuid = UUID.randomUUID().toString();
            lines.add(i, new Line("capcha_" + uuid.substring(0, uuid.indexOf("-")), x1, y1, x2, y2, THICKNESS, Color.WHITE, Color.WHITE));
        }
        return lines;
    }

    private static float wiggle() {
        return (float) (Math.random() * WIGGLE) * (Math.random() > .5 ? -1 : 1);
    }
}
