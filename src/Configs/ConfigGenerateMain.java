package Configs;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class ConfigGenerateMain {

    public static void main(String[] args) {
        Scenario defaultScenario = new Scenario(0, "this is a description",
                                                0.25, 0.0625,
                                                100, 3827);
        SimulationParams params = new SimulationParams(0, defaultScenario, 1000);
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File("Configs/resources/SimulationConfigs.cfg"), params);

            SimulationParams params2 = new ObjectMapper().readValue(new File("Configs/resources/SimulationConfigs.cfg"), SimulationParams.class);
            System.out.println(params2.hostID);
            System.out.println(params2.scenario);
            System.out.println(params2.maxBundleSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
