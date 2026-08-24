package com.example.wardrobe.description;

import com.example.wardrobe.garment.entity.Garment;

public interface DescriptionGenerator {
    String generate(Garment garment);
}
