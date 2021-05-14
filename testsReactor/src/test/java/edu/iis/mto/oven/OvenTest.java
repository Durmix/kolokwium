package edu.iis.mto.oven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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

    private final int INITIAL_TEMP_POSITIVE = 50;
    private final int INITIAL_TEMP_NEGATIVE = -10;
    private ProgramStage stageOne;
    private ProgramStage stageTwo;
    private ProgramStage stageThree;

    @BeforeEach
    void setUp() {
        oven = new Oven(heatingModule, fan);
        int temp = 180;
        int time = 25;
        heatingSettings = HeatingSettings.builder().withTargetTemp(temp).withTimeInMinutes(time).build();
        stageOne = ProgramStage.builder()
                .withHeat(HeatType.HEATER).withTargetTemp(temp).withStageTime(time).build();
        stageTwo = ProgramStage.builder()
                .withHeat(HeatType.THERMO_CIRCULATION).withTargetTemp(temp).withStageTime(time).build();
        stageThree = ProgramStage.builder()
                .withHeat(HeatType.GRILL).withTargetTemp(temp).withStageTime(time).build();
    }

    @Test
    void programShouldCallHeatingModuleHeaterWithGivenSettings() {
        List<ProgramStage> stages = List.of(stageOne);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(INITIAL_TEMP_POSITIVE).withStages(stages).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule).heater(heatingSettings);
    }

    @Test
    void programShouldCallHeatingModuleHeaterAndTermalCircuitWithGivenSettings_ShouldTurnOnAndOffTheFan() throws HeatingException {
        List<ProgramStage> stages = List.of(stageTwo);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(INITIAL_TEMP_POSITIVE).withStages(stages).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule).heater(HeatingSettings.builder()
                .withTargetTemp(INITIAL_TEMP_POSITIVE).withTimeInMinutes(0).build());
        Mockito.verify(heatingModule).termalCircuit(heatingSettings);
        Mockito.verify(fan).on();
        Mockito.verify(fan).off();
    }

    @Test
    void programShouldCallHeatingModuleTermalCircuitWithGivenSettings_ShouldTurnOnAndOffTheFan() throws HeatingException {
        List<ProgramStage> stages = List.of(stageTwo);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(INITIAL_TEMP_NEGATIVE).withStages(stages).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule).termalCircuit(heatingSettings);
        Mockito.verify(fan).on();
        Mockito.verify(fan).off();
    }

    @Test
    void programShouldCallHeatingModuleHeaterAndGrillWithGivenSettings_ShouldTurnOffTheFan() throws HeatingException {
        List<ProgramStage> stages = List.of(stageThree);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(INITIAL_TEMP_POSITIVE).withStages(stages).build();
        Mockito.when(fan.isOn()).thenReturn(true);

        oven.start(bakingProgram);

        Mockito.verify(heatingModule).heater(HeatingSettings.builder()
                .withTargetTemp(INITIAL_TEMP_POSITIVE).withTimeInMinutes(0).build());
        Mockito.verify(heatingModule).grill(heatingSettings);
        Mockito.verify(fan).isOn();
        Mockito.verify(fan).off();
    }

    @Test
    void programShouldCallHeatingModuleGrillWithGivenSettings_ShouldTurnOffTheFan() throws HeatingException {
        List<ProgramStage> stages = List.of(stageThree);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(INITIAL_TEMP_NEGATIVE).withStages(stages).build();
        Mockito.when(fan.isOn()).thenReturn(true);

        oven.start(bakingProgram);

        Mockito.verify(heatingModule).grill(heatingSettings);
        Mockito.verify(fan).isOn();
        Mockito.verify(fan).off();
    }

    @Test
    void programShouldCallGivenMethodsInCorrectOrder() throws HeatingException {
        List<ProgramStage> stages = List.of(stageOne, stageTwo, stageThree);
        bakingProgram = BakingProgram.builder()
                .withInitialTemp(INITIAL_TEMP_POSITIVE).withStages(stages).build();
        Mockito.when(fan.isOn()).thenReturn(true);

        oven.start(bakingProgram);

        InOrder order = Mockito.inOrder(heatingModule, fan);
        order.verify(fan).isOn();
        order.verify(heatingModule).heater(heatingSettings);
        order.verify(fan).on();
        order.verify(heatingModule).termalCircuit(heatingSettings);
        order.verify(fan).off();
        order.verify(fan).isOn();
        order.verify(heatingModule).grill(heatingSettings);
    }

}
