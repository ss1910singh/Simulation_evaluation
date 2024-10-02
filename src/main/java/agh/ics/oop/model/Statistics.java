package agh.ics.oop.model;

import agh.ics.oop.model.world_elements.GenomeView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import lombok.Getter;

@Getter
public class Statistics {
    private final ObjectProperty<Integer> dayNumber = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> freePositions = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> animalsCount = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> deadAnimalsCount = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> grassCount = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> waterCount = new SimpleObjectProperty<>();
    private final ObjectProperty<Double> averageEnergy = new SimpleObjectProperty<>();
    private final ObjectProperty<Double> averageLifeSpan = new SimpleObjectProperty<>();
    private final ObjectProperty<Double> averageChildrenCount = new SimpleObjectProperty<>();
    private final ObjectProperty<GenomeView> dominateGenome = new SimpleObjectProperty<>();

    public String getCsvNames() {
        return "dayNumber;" +
                "freePositions;" +
                "animalsCount;" +
                "deadAnimalsCount;" +
                "grassCount;" +
                "waterCount;" +
                "averageEnergy;" +
                "averageLifeSpan;" +
                "averageChildrenCount;" +
                "dominateGenome";
    }

    @Override
    public String toString() {
        return dayNumber.get() + ";" +
                freePositions.get() + ";" +
                animalsCount.get() + ";" +
                deadAnimalsCount.get() + ";" +
                grassCount.get() + ";" +
                waterCount.get() + ";" +
                averageEnergy.get() + ";" +
                averageLifeSpan.get() + ";" +
                averageChildrenCount.get() + ";" +
                dominateGenome.get();
    }
}
