package net.rebby.rebbys_nuclear_explosion.util;


import com.ibm.icu.impl.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Timeline {
    private final List<Pair<Float, Float[]>> entries;
    private final List<InterpolationMethod> interpolationMethods;
    private final int dimensionality;

    public Timeline(int dimensionality) {
        this(dimensionality, Pair.of(0.0f, new Float[dimensionality]));
        entries.getFirst().second[0] = 0.0f;
    }

    public Timeline(Pair<Float, Float[]> initialValue) {
        this.dimensionality = initialValue.second.length;
        this.entries = new ArrayList<>();
        entries.add(initialValue);
        interpolationMethods = new ArrayList<>();
    }

    private Timeline(int dimensionality, Pair<Float, Float[]> initialValue) {
        this.dimensionality = dimensionality;
        this.entries = new ArrayList<>();
        entries.add(initialValue);
        interpolationMethods = new ArrayList<>();
    }

    public Timeline pushEntry(@NotNull Pair<Float, Float[]> entry, @NotNull InterpolationMethod interpolationMethod) {
        if (entry.second.length != dimensionality) {
            throw new ClassCastException("Cannot add entry since "+entry.second.length+ " is not of the same dimensionality "+dimensionality);
        }
        entries.add(entry);
        interpolationMethods.add(interpolationMethod);
        entries.sort(Comparator.comparing(e -> e.first));
        return this;
    }

    public Float[] interpolate(float t) {
        Comparator<Pair<Float, Float[]>> c = Comparator.comparing(o -> o.first);

        int index = Collections.binarySearch(entries, Pair.of(t, new Float[]{}), c);
        index = index < 0 ? index * -1 - 1 : index;

        if (index == 0) {
            return entries.getFirst().second.clone();
        } else if (index == entries.size()) {
            return entries.getLast().second.clone();
        }

        Float[] interpolated = new Float[dimensionality];
        float t0 = t - entries.get(index - 1).first;
        float easedT = interpolationMethods.get(index - 1).run(t0 /
                (entries.get(index).first - entries.get(index - 1).first));
        for (int d = 0; d < dimensionality; d++) {
            interpolated[d] = ((entries.get(index).second[d] - entries.get(index-1).second[d]) * easedT) + entries.get(index-1).second[d];
        }
        return interpolated.clone();
    }

    public interface InterpolationMethod {
        Float run(Float t);

        InterpolationMethod HOLD = t -> 0.0f;
        InterpolationMethod LINEAR = t -> t;
        InterpolationMethod EASE_IN = t -> t * t;
        InterpolationMethod EASE_OUT = t -> (float) (1 - Math.sqrt(1 - t));
        InterpolationMethod CUBIC_EASE_IN = t -> t * t * t;
        InterpolationMethod CUBIC_EASE_OUT = t -> (float) (1 - Math.pow(1 - t, 3));
    }
}
