package com.diggydwarff.tobacconistmod.compat.create;

/** Installs loader-safe homogenization status reporting for the common client overlay. */
public final class CreateHomogenizationCompat {
    private CreateHomogenizationCompat() {}

    public static void register() {
        CreateCompat.installHomogenizationStatusResolver((level, pos) -> {
            CreateTobaccoHomogenization.HomogenizationStatus status =
                    CreateTobaccoHomogenization.getStatus(level, pos);
            if (!status.relevant()) {
                return CreateCompat.HomogenizationStatus.NONE;
            }
            return new CreateCompat.HomogenizationStatus(
                    true,
                    status.count(),
                    status.target(),
                    status.averageQuality(),
                    status.predictedQuality(),
                    status.ready(),
                    status.signalStrength(),
                    status.processing(),
                    status.finishMode(),
                    status.finishArmed(),
                    status.incompatibleCount(),
                    status.uniform()
            );
        });
    }
}
