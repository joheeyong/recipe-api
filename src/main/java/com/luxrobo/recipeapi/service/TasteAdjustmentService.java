package com.luxrobo.recipeapi.service;

import com.luxrobo.recipeapi.entity.RecipeIngredient;
import com.luxrobo.recipeapi.entity.RecipeStep;
import com.luxrobo.recipeapi.entity.UserPreference;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TasteAdjustmentService {

    // 재료명 → 맛 카테고리 매핑
    private static final Map<String, String> INGREDIENT_TASTE_MAP = new LinkedHashMap<>();

    static {
        // 매운맛 (spicy)
        for (String k : List.of("고춧가루", "고추장", "고추", "청양고추", "라면 스프", "카레"))
            INGREDIENT_TASTE_MAP.put(k, "spicy");
        // 단맛 (sweet)
        for (String k : List.of("설탕", "올리고당", "꿀", "물엿", "배즙", "케첩"))
            INGREDIENT_TASTE_MAP.put(k, "sweet");
        // 짠맛 (salty)
        for (String k : List.of("간장", "국간장", "소금", "새우젓", "된장", "쌈장", "춘장"))
            INGREDIENT_TASTE_MAP.put(k, "salty");
        // 신맛 (sour)
        for (String k : List.of("식초", "레몬즙", "레몬", "매실액", "김치"))
            INGREDIENT_TASTE_MAP.put(k, "sour");
        // 감칠맛 (umami)
        for (String k : List.of("멸치", "다시마", "표고버섯", "들깨가루", "참치액", "굴소스"))
            INGREDIENT_TASTE_MAP.put(k, "umami");
        // 기름기 (oily)
        for (String k : List.of("식용유", "참기름", "들기름", "버터", "올리브유"))
            INGREDIENT_TASTE_MAP.put(k, "oily");
    }

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("([0-9]+\\.?[0-9]*/?\\.?[0-9]*)\\s*(.*)");
    private static final Pattern FRACTION_PATTERN = Pattern.compile("(\\d+)/(\\d+)");

    /**
     * 사용자 입맛에 맞게 재료와 조리법을 조정합니다.
     * 기준값 5에 대해 비례 조정합니다.
     */
    public Map<String, Object> adjust(List<RecipeIngredient> ingredients,
                                       List<RecipeStep> steps,
                                       UserPreference pref) {
        List<Map<String, Object>> adjustedIngredients = new ArrayList<>();
        List<String> adjustmentNotes = new ArrayList<>();

        for (RecipeIngredient ing : ingredients) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ing.getId());
            item.put("recipeId", ing.getRecipeId());
            item.put("name", ing.getName());
            item.put("optional", ing.isOptional());

            String tasteCategory = findTasteCategory(ing.getName());
            if (tasteCategory != null) {
                int userLevel = getUserLevel(pref, tasteCategory);
                double ratio = computeRatio(userLevel);
                String adjusted = adjustAmount(ing.getAmount(), ratio);
                item.put("amount", adjusted);
                item.put("originalAmount", ing.getAmount());
                item.put("tasteCategory", tasteCategory);
                item.put("adjusted", ratio != 1.0);
            } else {
                item.put("amount", ing.getAmount());
                item.put("adjusted", false);
            }
            adjustedIngredients.add(item);
        }

        // 조리법 조정
        List<Map<String, Object>> adjustedSteps = new ArrayList<>();
        for (RecipeStep step : steps) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("id", step.getId());
            s.put("recipeId", step.getRecipeId());
            s.put("stepNumber", step.getStepNumber());
            s.put("instruction", step.getInstruction());
            s.put("imageUrl", step.getImageUrl());

            String tip = step.getTip() != null ? step.getTip() : "";
            String extraTip = generateStepTip(step.getInstruction(), pref);
            if (!extraTip.isEmpty()) {
                tip = tip.isEmpty() ? extraTip : tip + " | " + extraTip;
            }
            s.put("tip", tip.isEmpty() ? null : tip);
            adjustedSteps.add(s);
        }

        // 전체 조정 요약 노트
        buildAdjustmentNotes(adjustmentNotes, pref);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ingredients", adjustedIngredients);
        result.put("steps", adjustedSteps);
        result.put("adjustmentNotes", adjustmentNotes);
        return result;
    }

    private String findTasteCategory(String ingredientName) {
        for (Map.Entry<String, String> entry : INGREDIENT_TASTE_MAP.entrySet()) {
            if (ingredientName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private int getUserLevel(UserPreference pref, String category) {
        return switch (category) {
            case "spicy" -> pref.getSpicyLevel();
            case "sweet" -> pref.getSweetnessLevel();
            case "salty" -> pref.getSaltinessLevel();
            case "sour" -> pref.getSournessLevel();
            case "umami" -> pref.getUmamiLevel();
            case "oily" -> pref.getOilinessLevel();
            default -> 5;
        };
    }

    /**
     * 레벨에 따른 양 조정 비율 계산
     * level 1 = 0.5배, level 5 = 1.0배, level 10 = 1.5배
     * 선형 보간: ratio = 0.5 + (level - 1) * (1.0 / 9)
     */
    private double computeRatio(int level) {
        double ratio = 0.5 + (level - 1) * (1.0 / 9.0);
        return Math.round(ratio * 100.0) / 100.0;
    }

    /**
     * 양(amount) 문자열에서 숫자를 찾아 비율을 적용
     */
    private String adjustAmount(String amount, double ratio) {
        if (amount == null || amount.isEmpty() || ratio == 1.0) return amount;
        if (amount.equals("약간") || amount.equals("적당량") || amount.equals("넉넉히")) {
            if (ratio < 0.8) return "조금만";
            if (ratio > 1.2) return "넉넉히";
            return amount;
        }

        Matcher fractionMatcher = FRACTION_PATTERN.matcher(amount);
        if (fractionMatcher.find()) {
            double numerator = Double.parseDouble(fractionMatcher.group(1));
            double denominator = Double.parseDouble(fractionMatcher.group(2));
            double value = (numerator / denominator) * ratio;
            String unit = amount.substring(fractionMatcher.end()).trim();
            return formatAmount(value) + (unit.isEmpty() ? "" : unit);
        }

        Matcher matcher = AMOUNT_PATTERN.matcher(amount);
        if (matcher.matches()) {
            String numStr = matcher.group(1);
            String unit = matcher.group(2);
            try {
                double value = Double.parseDouble(numStr) * ratio;
                return formatAmount(value) + (unit.isEmpty() ? "" : unit);
            } catch (NumberFormatException e) {
                return amount;
            }
        }
        return amount;
    }

    private String formatAmount(double value) {
        if (value == Math.floor(value) && value < 100) {
            return String.valueOf((int) value);
        }
        // 소수점 1자리까지
        String formatted = String.format("%.1f", value);
        if (formatted.endsWith(".0")) {
            return formatted.substring(0, formatted.length() - 2);
        }
        return formatted;
    }

    /**
     * 조리법 단계에서 양념 관련 키워드를 감지하고 입맛에 맞는 팁 생성
     */
    private String generateStepTip(String instruction, UserPreference pref) {
        List<String> tips = new ArrayList<>();

        if (pref.getSpicyLevel() > 7 && containsAny(instruction, "고춧가루", "고추장", "고추")) {
            tips.add("매운맛을 좋아하시면 고춧가루를 더 넣어보세요");
        } else if (pref.getSpicyLevel() < 3 && containsAny(instruction, "고춧가루", "고추장", "고추")) {
            tips.add("매운맛이 부담되면 양을 줄이거나 빼셔도 돼요");
        }

        if (pref.getSweetnessLevel() > 7 && containsAny(instruction, "설탕", "올리고당")) {
            tips.add("단맛을 좋아하시면 조금 더 넣어보세요");
        } else if (pref.getSweetnessLevel() < 3 && containsAny(instruction, "설탕", "올리고당")) {
            tips.add("단맛을 줄이고 싶으면 양을 반으로 줄여보세요");
        }

        if (pref.getSaltinessLevel() < 3 && containsAny(instruction, "간장", "소금", "새우젓", "된장")) {
            tips.add("싱겁게 드시려면 간을 약하게 해주세요");
        } else if (pref.getSaltinessLevel() > 7 && containsAny(instruction, "간장", "소금", "새우젓", "된장")) {
            tips.add("간을 좀 더 세게 하셔도 좋아요");
        }

        if (pref.getOilinessLevel() < 3 && containsAny(instruction, "기름", "식용유", "참기름")) {
            tips.add("기름기를 줄이시려면 기름 양을 줄여주세요");
        }

        return String.join(" / ", tips);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private void buildAdjustmentNotes(List<String> notes, UserPreference pref) {
        if (pref.getSpicyLevel() != 5) {
            notes.add(pref.getSpicyLevel() > 5
                    ? "🌶️ 매운맛 선호에 맞춰 매운 재료를 늘렸어요"
                    : "🌶️ 순한맛 선호에 맞춰 매운 재료를 줄였어요");
        }
        if (pref.getSweetnessLevel() != 5) {
            notes.add(pref.getSweetnessLevel() > 5
                    ? "🍯 단맛 선호에 맞춰 단맛 재료를 늘렸어요"
                    : "🍯 단맛을 줄인 레시피로 조정했어요");
        }
        if (pref.getSaltinessLevel() != 5) {
            notes.add(pref.getSaltinessLevel() > 5
                    ? "🧂 짠맛 선호에 맞춰 간을 세게 조정했어요"
                    : "🧂 싱거운 맛 선호에 맞춰 간을 약하게 조정했어요");
        }
        if (pref.getSournessLevel() != 5) {
            notes.add(pref.getSournessLevel() > 5
                    ? "🍋 신맛 선호에 맞춰 신맛 재료를 늘렸어요"
                    : "🍋 신맛을 줄인 레시피로 조정했어요");
        }
        if (pref.getUmamiLevel() != 5) {
            notes.add(pref.getUmamiLevel() > 5
                    ? "🍄 감칠맛 선호에 맞춰 감칠맛 재료를 늘렸어요"
                    : "🍄 담백한 맛 선호에 맞춰 감칠맛 재료를 줄였어요");
        }
        if (pref.getOilinessLevel() != 5) {
            notes.add(pref.getOilinessLevel() > 5
                    ? "🫒 기름진 맛 선호에 맞춰 기름 양을 늘렸어요"
                    : "🫒 담백한 맛 선호에 맞춰 기름 양을 줄였어요");
        }
    }
}
