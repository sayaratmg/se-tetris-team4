package component;

import java.awt.Color;

public class ColorBlindPalette {

    public enum Mode { NORMAL, PROTAN, DEUTER, TRITAN }

    public static Color convert(Color original, Mode mode) {
        if (mode == Mode.NORMAL) return original;

        float[] hsv = Color.RGBtoHSB(original.getRed(), original.getGreen(), original.getBlue(), null);
        float h = hsv[0] * 360;
        float s = hsv[1];
        float v = hsv[2];

        switch (mode) {
            case PROTAN -> { // 적색맹
                if (h >= 0 && h <= 60) h = (h + 40) % 360;     // 빨강→주황
                if (h >= 120 && h <= 180) h = (h + 60) % 360;  // 녹색→청록
                v = Math.min(1.0f, v * 1.1f);                  // 어두운 영역 보정
            }
            case DEUTER -> { // 녹색맹
                if (h >= 100 && h <= 160) h = (h + 70) % 360;  // 녹색→청록
                if (h >= 0 && h <= 60) h = (h + 30) % 360;     // 빨강→주황
                v = Math.min(1.0f, v * 1.15f);
            }
            case TRITAN -> { // 청황색맹
                if (h >= 180 && h <= 300) h = (h - 40 + 360) % 360; // 파랑→보라
                v = Math.min(1.0f, v * 1.05f);
            }
        }
        return Color.getHSBColor(h / 360f, s, v);
    }
}
