package Configs;

import SendStrings.App;

import java.security.InvalidParameterException;
import java.util.Arrays;

import static SendStrings.App.simulationids;

/**
 * Parameters involving the Scenario
 * @param scenarioID ID of scenario this represents
 * @param description description of scenario
 * @param expectedDownProbability The probability of a link having an expected disconnect/ going "down".
 * @param unexpectedDownProbability The probability of a link having an unexpected disconnect/ going "down".
 * @param milliPerDownPeriod The number of milliseconds per Timeframe
 * @param dtcpPort Which port DTCP listens on, (use 3827 by default, which is DTCP in a keypad lettering scheme)
 */

public record Scenario(int scenarioID, String description, double expectedDownProbability,
                       double unexpectedDownProbability, int milliPerDownPeriod, int dtcpPort,
                       int bundleLifetimeMS) {


    // Parameter Checking Variables
    /**
     * below 1024 are reserved, ports only go to 2^16-1
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static final int MIN_PORT = 1024, MAX_PORT = 1 << 16, MIN_BUNDLE_LIFETIME = 50, MAX_BUNDLE_LIFETIME = 100000;

    /**
     * Validates simulation parameters
     */
    public Scenario {
        // Check parameters
        if (Arrays.stream(simulationids).noneMatch(id -> id == scenarioID))
            throw new InvalidParameterException("Scenario: ScenarioID must be in " + Arrays.toString(simulationids));
        if (description == null)
            throw new InvalidParameterException("Scenario: Description cannot be null");
        if (expectedDownProbability < 0 || expectedDownProbability >= 1)
            throw new InvalidParameterException("Scenario: expectedDownProbability must be in [0,1)");
        if (unexpectedDownProbability < 0 || unexpectedDownProbability >= 1)
            throw new InvalidParameterException("Scenario: unexpectedDownProbability must be in [0,1)");
        if (dtcpPort <= MIN_PORT || dtcpPort >= MAX_PORT)
            throw new InvalidParameterException("Scenario: invalid port, must be in [1024,65524]");
        if(bundleLifetimeMS < MIN_BUNDLE_LIFETIME || bundleLifetimeMS >= MAX_BUNDLE_LIFETIME)
            throw new InvalidParameterException("Scenario: invalid bundle lifetime, must be in " +
                    "[" + MIN_BUNDLE_LIFETIME + ", " + MAX_BUNDLE_LIFETIME + "]");
    }

    /** @return concise toString */
    public String toStringShort() { return this.scenarioID + ":: \"" + this.description + "\""; }
}
