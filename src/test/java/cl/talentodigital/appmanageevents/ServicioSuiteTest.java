package cl.talentodigital.appmanageevents;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({SalonServicioTest.class, EventoServicioTest.class})
class ServicioSuiteTest {
    // Test suite for SalonServicioTest and EventoServicioTest
    // Run this class to run both test classes
    // This class should not contain any logic

    @Test
    void testSuiteRuns() {
        // This test just ensures that the suite runs
        assertTrue(true, "The test suite is running.");
    }
}
