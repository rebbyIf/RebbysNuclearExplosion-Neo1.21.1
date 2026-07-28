package net.rebby.rebbys_nuclear_explosion.util;


import com.ibm.icu.impl.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Timeline {
    private List<Pair<Float, Float[]>> entries;
    private List<InterpolationMethod> interpolationMethods;
    private int dimensionality;

    public Timeline(int dimensionality) {
        Pair<Float, Float[]> initialValue = Pair.of(0.0f, new Float[dimensionality]);
        new Timeline(dimensionality, initialValue);
    }

    public Timeline(Pair<Float, Float[]> initialValue) {
        this.dimensionality = initialValue.second.length;
        this.entries = new ArrayList<>();
        entries.add(initialValue);
        interpolationMethods = new ArrayList<>();
    }

    public Timeline(int dimensionality, Pair<Float, Float[]> initialValue) {
        this.dimensionality = dimensionality;
        this.entries = new ArrayList<>();
        entries.add(initialValue);
        interpolationMethods = new ArrayList<>();
    }

    public Timeline pushEntry(@NotNull Pair<Float, Float[]> entry, @NotNull InterpolationMethod interpolationMethod) {
        if (entry.second.length != dimensionality) {
            throw new ClassCastException("Cannot add entry since it's value is not of the same dimensionality!");
        }
        entries.add(entry);
        interpolationMethods.add(interpolationMethod);
        entries.sort((e1, e2) -> e2.first.compareTo(e1.first));
        return this;
    }

    public Float[] interpolate(float t) {
        Comparator<Pair<Float, Float[]>> c = new Comparator<Pair<Float, Float[]>>() {
            @Override
            public int compare(Pair<Float, Float[]> o1, Pair<Float, Float[]> o2) {
                return o2.first.compareTo(o1.first);
            }
        };

        int index = Collections.binarySearch(entries, Pair.of(t, new Float[]{}), c);
        index = index < 0 ? index * -1 - 1 : index;

        if (index == 0) {
            return entries.getFirst().second.clone();
        }if (index == entries.size()) {
            return entries.getLast().second.clone();
        }

        Float[] interpolated = new Float[dimensionality];
        float t0 = t - entries.get(index - 1).first;
        float easedT = interpolationMethods.get(index - 1).run(t0 /
                (entries.get(index).first - entries.get(index - 1).first));
        for (int d = 0; d < dimensionality; d++) {
            interpolated[d] = ((entries.get(index).second[d] - entries.get(index-1).second[d]) * easedT) + entries.get(index-1).second[d];
        }
        return interpolated;
    }

    public interface InterpolationMethod {
        Float run(Float t);

        InterpolationMethod HOLD = t -> 0.0f;
        InterpolationMethod LINEAR = t -> t;
        InterpolationMethod EASE_IN = t -> t * t;
        InterpolationMethod EASE_OUT = t -> (float) (1 - Math.sqrt(1 - t));
    }
}
