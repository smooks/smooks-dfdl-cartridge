package org.smooks.cartridges.dfdl;

import org.apache.daffodil.japi.ValidationMode;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class TestKit {
    private TestKit() {

    }

    public static <T extends Object> T getRandomItem(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static List<ValidationMode> getCacheOnDiskSupportedValidationModes() {
        return Arrays.asList(ValidationMode.values()).stream().filter(v -> !v.equals(ValidationMode.Full)).collect(Collectors.toList());
    }
}
