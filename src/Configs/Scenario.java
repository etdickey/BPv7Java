package Configs;

/**
 * todo:: Aidan -- use these params to determine what to do at the DTCP layer
 * todo:: Ethan -- store variables you might need at the top layer as well
 * @param scenarioID ID of scenario this represents
 * @param description description of scenario
 * @param numDisruptions filler var
 * @param disruptFrequency filler var
 */
public record Scenario(int scenarioID, String description, int numDisruptions, double disruptFrequency) {
    /** @return concise toString */
    public String toStringShort() { return this.scenarioID + ":: \"" + this.description + "\""; }
}
