package io.roastedroot.sqlite4j.core.wasm;

import io.roastedroot.sqlite4j.Function;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class UDFStore {
    // we need to avoid returning 0 to disambiguate with NULL
    private static int OFFSET = 1;
    private static int MIN_CAPACITY = 8;
    private static int count;
    private static ArrayDeque<Integer> emptySlots = new ArrayDeque<>();
    private static Function[] store = new Function[MIN_CAPACITY];
    private static Map<String, Integer> names = new HashMap<>();

    private static void increaseCapacity() {
        final int newCapacity = store.length << 1;

        final Function[] array = new Function[newCapacity];
        System.arraycopy(store, 0, array, 0, store.length);

        store = array;
    }

    public static int registerFunction(String name, Function f) {
        int result;
        if (emptySlots.isEmpty()) {
            store[count] = f;
            count++;

            if (count == store.length) {
                increaseCapacity();
            }
            result = (count - 1);
        } else {
            int emptySlot = emptySlots.pop();
            // just a sanity check
            assert (store[emptySlot] == null);

            store[emptySlot] = f;
            result = emptySlot;
        }
        names.put(name, result);
        return result + OFFSET;
    }

    public static void free(String name) {
        if (names.containsKey(name)) {
            free(names.get(name) + OFFSET);
            names.remove(name);
        }
    }

    public static void free(int idx) {
        idx = idx - OFFSET;
        store[idx] = null;
        emptySlots.push(idx);
    }

    public static Function get(int idx) {
        idx = idx - OFFSET;
        return store[idx];
    }
}
