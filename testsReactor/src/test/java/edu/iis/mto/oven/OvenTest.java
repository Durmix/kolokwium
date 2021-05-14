package edu.iis.mto.oven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class OvenTest {

    @Mock
    private BakingProgram bakingProgram;
    @Mock
    private HeatingModule heatingModule;
    @Mock
    private HeatingSettings heatingSettings;
    @Mock
    private Fan fan;

    private Oven oven;

    @BeforeEach
    void setUp() {
        oven = new Oven(heatingModule, fan);
    }

    @Test
    void programShouldCallHeatingModuleHeaterWithGivenSettings() {
        int targetTemp = 180;
        int irrelevant = 50;
        int time = 25;
        heatingSettings = HeatingSettings.builder().withTargetTemp(targetTemp).withTimeInMinutes(time).build();
        ProgramStage stageOne = ProgramStage.builder()
                .withHeat(HeatType.HEATER).withTargetTemp(targetTemp).withStageTime(time).build();
        List<ProgramStage> stages = List.of(stageOne);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(irrelevant).withStages(stages).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule).heater(heatingSettings);
    }

    @Test
    void programShouldCallHeatingModuleTermalCircuitWithGivenSettings_ShouldTurnOnAndOffTheFan() throws HeatingException {
        int targetTemp = 180;
        int irrelevant = 50;
        int time = 25;
        heatingSettings = HeatingSettings.builder().withTargetTemp(targetTemp).withTimeInMinutes(time).build();
        ProgramStage stageOne = ProgramStage.builder()
                .withHeat(HeatType.THERMO_CIRCULATION).withTargetTemp(targetTemp).withStageTime(time).build();
        List<ProgramStage> stages = List.of(stageOne);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(irrelevant).withStages(stages).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule).termalCircuit(heatingSettings);
        Mockito.verify(fan).on();
        Mockito.verify(fan).off();
    }

}
