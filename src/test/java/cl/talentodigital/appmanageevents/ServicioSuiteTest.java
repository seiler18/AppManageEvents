package cl.talentodigital.appmanageevents;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({SalonServicioTest.class, EventoServicioTest.class})
public class ServicioSuiteTest {
    // Test suite for SalonServicioTest and EventoServicioTest
    // Run this class to run both test classes
    // This class should not contain any logic
}
