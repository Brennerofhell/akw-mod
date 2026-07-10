package ch.danielt.akw.reactor;

import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WordSplittingTest {

    @Test
    public void testWordSplitting() {
        int[] testValues = {
            0,
            1,
            100,
            32767,
            32768,
            65535,
            65536,
            100000,
            20000000,
            Integer.MAX_VALUE
        };

        for (int val : testValues) {
            int low = NuclearReactorBlockEntity.getLowWord(val);
            int high = NuclearReactorBlockEntity.getHighWord(val);
            int combined = NuclearReactorBlockEntity.combineWords(low, high);
            Assertions.assertEquals(val, combined, "Value mismatch for: " + val);
        }
    }
}
