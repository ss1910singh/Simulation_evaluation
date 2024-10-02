package agh.ics.oop.model.world_elements;

import agh.ics.oop.model.ModelConfiguration;
import agh.ics.oop.model.world_map.IMoveHandler;
import agh.ics.oop.model.world_map.MapDirection;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static agh.ics.oop.model.util.MathUtil.clamp;
import static agh.ics.oop.model.util.MathUtil.getColorGradient;
import static java.lang.Math.max;

@ToString(onlyExplicitlyIncluded = true)
public class Animal implements IWorldElement, Comparable<Animal> {
    @Getter
    private Vector2d position;
    @Getter
    private MapDirection orientation;
    @Getter
    @ToString.Include
    private int energyLevel;
    private final Genome genome;
    private final ModelConfiguration configuration;
    @Getter
    @ToString.Include
    private int age;
    private final List<Animal> children;
    @Getter
    private int grassEaten = 0;
    @Getter
    @Setter
    private int diedAt = -1;
    @Getter
    private String deathReason = "";

    public Animal(int initialEnergyLevel, Vector2d initialPosition, ModelConfiguration configuration){
        this.children = new ArrayList<>();
        this.configuration = configuration;

        this.genome = Genome.RandomGenome(this.configuration.getGenomeLength(), this.configuration.getConstructedBehaviour());
        initializeAnimal(initialEnergyLevel, initialPosition);
    }

    private Animal(int initialEnergyLevel, Genome initialGenome, Vector2d initialPosition, ModelConfiguration configuration) {
        this.children = new ArrayList<>();
        this.configuration = configuration;

        this.genome = initialGenome;
        initializeAnimal(initialEnergyLevel, initialPosition);
    }

    private void initializeAnimal(int initialEnergyLevel, Vector2d initialPosition){
        this.energyLevel = initialEnergyLevel;
        this.position = initialPosition;
        this.orientation = MapDirection.values()[ThreadLocalRandom.current().nextInt(0, MapDirection.values().length)];
        this.age = 0;
    }

    @Override
    public boolean isAt(Vector2d position) {
        return position.equals(this.position);
    }

    public void move(IMoveHandler moveHandler){
        orientation = orientation.shift(genome.getActiveGene().ordinal());
        Vector2d newPosition = moveHandler.getTranslatedPosition(position.add(orientation.toUnitVector()));

        if(moveHandler.canMoveTo(newPosition)){
            position = newPosition;
        } else {
            orientation = orientation.shift(Gene.ROTATION_180.ordinal());
        }

        this.genome.nextGene();
        this.energyLevel -= this.configuration.getAnimalEnergyLossPerMove();

        if(this.energyLevel <= 0)
            this.deathReason = "Starvation";
    }

    public void age(){
        this.age++;
    }

    public void eat(){
        energyLevel += this.configuration.getGrassEnergyLevel();
        grassEaten++;
    }

    public boolean isAlive() {
        return energyLevel > 0;
    }

    @Override
    public int compareTo(Animal o) {
        return Integer.compare(energyLevel, o.energyLevel);
    }

    public double getHealth() {
        double percent = (double)energyLevel/configuration.getAnimalStartingEnergy();
        return clamp(percent, 0.0, 1.0);
    }

    public Animal breed(Animal other){
        int side = ThreadLocalRandom.current().nextInt(0, 2);

        float genesRatio = max(this.energyLevel, other.energyLevel)/(float)(this.energyLevel + other.energyLevel);

        Genome newGenome = getChildGenome(other, side, genesRatio);

        this.energyLevel -= this.configuration.getAnimalEnergyGivenToChild();
        other.energyLevel -= this.configuration.getAnimalEnergyGivenToChild();

        if(this.energyLevel <= 0)
            this.deathReason = "Maternal death";
        if(other.energyLevel <= 0)
            other.deathReason = "Maternal death";

        Animal child = new Animal(2*this.configuration.getAnimalEnergyGivenToChild(), newGenome, other.getPosition(), this.configuration);
        this.children.add(child);

        return child;
    }

    private Genome getChildGenome(Animal other, int side, float genesRatio) {
        Genome strongerGenome = this.energyLevel > other.energyLevel ? this.genome : other.genome;
        Genome weakerGenome = this.energyLevel > other.energyLevel ? other.genome : this.genome;

        Genome newGenome = switch (side) {
            case 0 -> strongerGenome.combineGenomes(weakerGenome, genesRatio);
            case 1 -> weakerGenome.combineGenomes(strongerGenome, 1 - genesRatio);
            default -> throw new IllegalStateException("Unexpected value: " + side);
        };

        newGenome.mutate(this.configuration.getMinimalMutationsCount(), this.configuration.getMaximalMutationsCount());
        return newGenome;
    }

    public boolean canBreed() {
        return energyLevel > this.configuration.getAnimalReadyToBreedEnergyLevel();
    }

    public void die(int day, String deathReason){
        this.energyLevel = 0;
        this.diedAt = day;
        this.deathReason = deathReason;
    }

    public GenomeView getGenomeView() {
        return new GenomeView(genome);
    }

    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public String getResourceName() {
        return "owlbear.png";
    }

    @Override
    public Color getColor() {
        return getColorGradient(getHealth(), Color.RED, Color.LIME);
    }

    public int getDescendantsCount() {
        Animal current = this;
        int descendantsCount = 0;
        Stack<Animal> animals = new Stack<>();
        Set<Animal> counted = new HashSet<>();

        animals.push(current);

        while(!animals.isEmpty()){
            current = animals.pop();
            counted.add(current);

            for(Animal child : current.children){
                if(!counted.contains(child)){
                    animals.push(child);
                    descendantsCount++;
                }
            }
        }

        return descendantsCount;
    }
}
