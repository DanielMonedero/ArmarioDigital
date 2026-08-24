package com.example.wardrobe.description;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Color;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateDescriptionGeneratorTest {

    private final TemplateDescriptionGenerator generator = new TemplateDescriptionGenerator();

    @Test
    void generatesFullDescription() {
        Garment g = garment("Camiseta", "Nike", Color.BLUE, "M", GarmentCondition.GOOD);
        assertThat(generator.generate(g)).isEqualTo("Camiseta Nike blue, talla M. En buen estado.");
    }

    @Test
    void omitsBrandWhenMissing() {
        Garment g = garment("Camiseta", null, Color.BLUE, "M", GarmentCondition.GOOD);
        assertThat(generator.generate(g)).isEqualTo("Camiseta blue, talla M. En buen estado.");
    }

    @Test
    void omitsColorWhenMissing() {
        Garment g = garment("Camiseta", "Nike", null, "M", GarmentCondition.GOOD);
        assertThat(generator.generate(g)).isEqualTo("Camiseta Nike, talla M. En buen estado.");
    }

    @Test
    void omitsSizeWhenMissing() {
        Garment g = garment("Camiseta", "Nike", Color.BLUE, null, GarmentCondition.GOOD);
        assertThat(generator.generate(g)).isEqualTo("Camiseta Nike blue. En buen estado.");
    }

    @Test
    void omitsSizeAndConditionWhenMissing() {
        Garment g = garment("Camiseta", "Nike", Color.BLUE, null, null);
        assertThat(generator.generate(g)).isEqualTo("Camiseta Nike blue.");
    }

    @Test
    void rendersDifferentConditions() {
        assertThat(generator.generate(garment("Camiseta", null, null, "M", GarmentCondition.NEW)))
                .isEqualTo("Camiseta, talla M. Nuevo a estrenar.");
        assertThat(generator.generate(garment("Camiseta", null, null, "M", GarmentCondition.LIKE_NEW)))
                .isEqualTo("Camiseta, talla M. Como nuevo.");
        assertThat(generator.generate(garment("Camiseta", null, null, "M", GarmentCondition.USED)))
                .isEqualTo("Camiseta, talla M. Usado.");
    }

    @Test
    void capitalizesName() {
        Garment g = garment("camiseta vintage", null, null, null, null);
        assertThat(generator.generate(g)).isEqualTo("Camiseta vintage.");
    }

    @Test
    void treatsBlankOptionalsAsMissing() {
        Garment g = garment("Camiseta", "  ", null, "   ", GarmentCondition.GOOD);
        assertThat(generator.generate(g)).isEqualTo("Camiseta. En buen estado.");
    }

    private Garment garment(String name, String brand, Color color, String size, GarmentCondition condition) {
        Garment g = new Garment();
        g.setOwner(new User());
        g.setName(name);
        g.setBrand(brand);
        g.setColor(color);
        g.setSize(size);
        g.setCondition(condition);
        g.setCategory(Category.TOP);
        g.setStatus(com.example.wardrobe.garment.entity.GarmentStatus.WARDROBE);
        g.setSeason(com.example.wardrobe.garment.entity.Season.SUMMER);
        return g;
    }
}
