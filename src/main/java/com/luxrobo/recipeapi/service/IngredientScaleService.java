package com.luxrobo.recipeapi.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class IngredientScaleService {

    // 비선형 스케일링 재료 (물, 육수 등 — 인분 늘어도 비례 증가하지 않음)
    private static final Set<String> SUBLINEAR_KEYWORDS = Set.of(
        "물", "육수", "다시", "국물", "멸치물", "다시마물", "사골육수",
        "치킨스톡", "비프스톡", "채수"
    );

    // 고정 재료 (양에 관계없이 변하지 않음)
    private static final Set<String> FIXED_KEYWORDS = Set.of(
        "식용유", "기름", "올리브유", "포도씨유", "참기름", "들기름",
        "버터", "마가린"
    );

    // 고정 수량 표현 (amount 기반)
    private static final Set<String> FIXED_AMOUNTS = Set.of(
        "약간", "적당량", "조금", "적당히", "소량", "취향껏", "기호에 맞게", "한줌"
    );

    /**
     * 재료 이름과 양을 기반으로 스케일 타입을 추론
     */
    public String inferScaleType(String name, String amount) {
        if (name == null) return "linear";

        String lowerName = name.trim().toLowerCase();
        String lowerAmount = amount != null ? amount.trim() : "";

        // 양이 정성적 표현이면 무조건 fixed
        for (String fixed : FIXED_AMOUNTS) {
            if (lowerAmount.contains(fixed)) return "fixed";
        }

        // 고정 재료 (기름류) — 소량일 때만 fixed (대량이면 linear)
        for (String keyword : FIXED_KEYWORDS) {
            if (lowerName.contains(keyword)) {
                // "3큰술" 이하면 fixed, 그 이상이면 linear
                return isSmallAmount(lowerAmount) ? "fixed" : "linear";
            }
        }

        // 물/육수류 → sublinear
        for (String keyword : SUBLINEAR_KEYWORDS) {
            if (lowerName.contains(keyword)) return "sublinear";
        }

        return "linear";
    }

    private boolean isSmallAmount(String amount) {
        if (amount.isEmpty()) return true;
        try {
            String numPart = amount.replaceAll("[^0-9.]", "");
            if (numPart.isEmpty()) return true;
            double value = Double.parseDouble(numPart);
            // ml/L 단위 체크
            if (amount.contains("ml") || amount.contains("ML")) return value <= 30;
            if (amount.contains("L") || amount.contains("l") || amount.contains("리터")) return false;
            // 큰술/작은술/컵 등
            return value <= 3;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
