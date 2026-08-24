package com.example.wardrobe.description;

import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TemplateDescriptionGenerator implements DescriptionGenerator {

    @Override
    public String generate(Garment garment) {
        String name = normalize(garment.getName());
        String brand = normalize(garment.getBrand());
        String color = normalize(garment.getColor());
        String size = normalize(garment.getSize());
        String conditionText = conditionText(garment.getCondition());

        StringBuilder head = new StringBuilder();
        head.append(capitalize(name));

        if (brand != null) {
            head.append(' ').append(brand);
        }
        if (color != null) {
            head.append(' ').append(color.toLowerCase(Locale.ROOT));
        }

        boolean hasSize = size != null;
        boolean hasCondition = conditionText != null;

        if (hasSize) {
            head.append(", talla ").append(size);
        }
        if (hasCondition) {
            if (hasSize) {
                head.append(". ").append(conditionText);
            } else {
                head.append(". ").append(conditionText);
            }
        }
        return head.append('.').toString();
    }

    private static String conditionText(GarmentCondition condition) {
        if (condition == null) {
            return null;
        }
        return switch (condition) {
            case NEW -> "Nuevo a estrenar";
            case LIKE_NEW -> "Como nuevo";
            case GOOD -> "En buen estado";
            case USED -> "Usado";
        };
    }

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
