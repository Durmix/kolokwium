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

    private final int TARGET_TEMP = 180;
    private final int IRRELEVANT = 50;
    private final int TIME = 25;

    @BeforeEach
    void setUp() {
        oven = new Oven(heatingModule, fan);
        heatingSettings = HeatingSettings.builder().withTargetTemp(TARGET_TEMP).withTimeInMinutes(TIME).build();
    }

    @Test
    void programShouldCallHeatingModuleHeaterWithGivenSettings() {
        ProgramStage stageOne = ProgramStage.builder()
                .withHeat(HeatType.HEATER).withTargetTemp(TARGET_TEMP).withStageTime(TIME).build();
        List<ProgramStage> stages = List.of(stageOne);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(IRRELEVANT).withStages(stages).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule).heater(heatingSettings);
    }

    @Test
    void programShouldCallHeatingModuleTermalCircuitWithGivenSettings_ShouldTurnOnAndOffTheFan() throws HeatingException {
        ProgramStage stageOne = ProgramStage.builder()
                .withHeat(HeatType.THERMO_CIRCULATION).withTargetTemp(TARGET_TEMP).withStageTime(TIME).build();
        List<ProgramStage> stages = List.of(stageOne);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(IRRELEVANT).withStages(stages).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule).termalCircuit(heatingSettings);
        Mockito.verify(fan).on();
        Mockito.verify(fan).off();
    }

    @Test
    void programShouldCallHeatingModuleGrillWithGivenSettings_ShouldTurnOffTheFan() throws HeatingException {
        ProgramStage stageOne = ProgramStage.builder()
                .withHeat(HeatType.GRILL).withTargetTemp(TARGET_TEMP).withStageTime(TIME).build();
        List<ProgramStage> stages = List.of(stageOne);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(IRRELEVANT).withStages(stages).build();
        Mockito.when(fan.isOn()).thenReturn(true);

        oven.start(bakingProgram);


        Mockito.verify(heatingModule).grill(heatingSettings);
        Mockito.verify(fan).isOn();
        Mockito.verify(fan).off();
    }

}
