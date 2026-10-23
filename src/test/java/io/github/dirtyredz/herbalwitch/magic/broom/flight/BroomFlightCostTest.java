package io.github.dirtyredz.herbalwitch.magic.broom.flight;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BroomFlightCostTest {
    @Test void everySupportedPriceChargesExactlyPerSecondAndPreservesRemainder() {
        for (int price = 0; price <= 2000; price++) {
            for (int initial = 0; initial < 20; initial++) {
                int remainder = initial, spent = 0;
                for (int tick = 0; tick < 20; tick++) {
                    var cost = BroomFlightCost.next(price, remainder);
                    spent += cost.units();
                    remainder = cost.remainder();
                }
                assertEquals(price, spent);
                assertEquals(initial, remainder);
            }
        }
    }

    @Test void rejectsMalformedCosts() {
        assertThrows(IllegalArgumentException.class, () -> BroomFlightCost.next(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> BroomFlightCost.next(2001, 0));
        assertThrows(IllegalArgumentException.class, () -> BroomFlightCost.next(1, 20));
    }
}
