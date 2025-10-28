package component;

import java.awt.Color;

/**
 * ColorBlindPalette
 * ---------------------
 * - 기본 팔레트: ColorBrewer 기반 7가지 블록 색
 * - 색각 모드 변환 지원: NORMAL / PROTAN / DEUTER / TRITAN
 * - Deuteranopia(녹색약) 시야에서도 명확히 구분되도록 조정
 */
public class ColorBlindPalette {

    public enum Mode { NORMAL, PROTAN, DEUTER, TRITAN }

    // === 기본 팔레트 (ColorBrewer 기반) ===
    public static final Color I = new Color(0x00, 0x96, 0xFF); // 하늘 파랑
    public static final Color J = new Color(0x78, 0x5E, 0xF0); // 보라 파랑
    public static final Color L = new Color(0xFF, 0x7F, 0x0E); // 오렌지
    public static final Color O = new Color(0xFF, 0xD7, 0x00); // 황금 노랑
    public static final Color S = new Color(0x2C, 0xA0, 0x2C); // 청록초록
    public static final Color T = new Color(0xD6, 0x27, 0x28); // 붉은보라
    public static final Color Z = new Color(0x8C, 0x56, 0x4B); // 갈색

    public static final Color[] BASE_COLORS = { I, J, L, O, S, T, Z };

    /**
     * 색각 모드에 맞게 색 변환
     * @param original 원본 색상
     * @param mode 색각 모드
     * @return 변환된 색상
     */
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
                v = Math.min(1.0f, v * 1.1f);
            }
            case DEUTER -> { // 녹색맹
                if (h >= 100 && h <= 160) h = (h + 70) % 360;  // 녹색→청록
                if (h >= 0 && h <= 60) h = 70;                 // 빨강→노랑
                v = Math.min(1.0f, v * 1.15f);
            }
            case TRITAN -> { // 청황색맹
                if (h >= 180 && h <= 300) h = (h - 40 + 360) % 360; // 파랑→보라
                v = Math.min(1.0f, v * 1.05f);
            }
        }

        return Color.getHSBColor(h / 360f, s, v);
    }

    /**
     * 팔레트 전체를 변환한 배열 반환
     */
    public static Color[] getPalette(Mode mode) {
        Color[] converted = new Color[BASE_COLORS.length];
        for (int i = 0; i < BASE_COLORS.length; i++) {
            converted[i] = convert(BASE_COLORS[i], mode);
        }
        return converted;
    }
}
