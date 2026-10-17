package com.example.model

data class VehicleModelInfo(
    val name: String,
    val suggestedPowertrain: VehiclePowertrain = VehiclePowertrain.COMBUSTAO,
    val averageConsumption: Double = 11.5,
    val electricConsumption: Double = 0.0
)

data class VehicleBrandInfo(
    val brandName: String,
    val models: List<VehicleModelInfo>
)

object VehicleCatalog {

    fun getDefaultConsumption(powertrain: VehiclePowertrain): Double {
        return when (powertrain) {
            VehiclePowertrain.COMBUSTAO -> 11.5
            VehiclePowertrain.HIBRIDO -> 16.5
            VehiclePowertrain.HIBRIDO_PLUGIN -> 18.0
            VehiclePowertrain.ELETRICO -> 6.5
        }
    }

    fun getDefaultElectricConsumption(powertrain: VehiclePowertrain): Double {
        return when (powertrain) {
            VehiclePowertrain.HIBRIDO_PLUGIN -> 5.5
            VehiclePowertrain.ELETRICO -> 6.5
            else -> 0.0
        }
    }

    fun getDefaultFuelPrice(powertrain: VehiclePowertrain): Double {
        return when (powertrain) {
            VehiclePowertrain.ELETRICO -> 0.85
            else -> 5.89
        }
    }

    val brands: List<VehicleBrandInfo> = listOf(
        VehicleBrandInfo(
            brandName = "Chevrolet",
            models = listOf(
                VehicleModelInfo("Onix", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Onix Plus (Sedan)", VehiclePowertrain.COMBUSTAO, 13.0),
                VehicleModelInfo("Prisma", VehiclePowertrain.COMBUSTAO, 12.0),
                VehicleModelInfo("Spin", VehiclePowertrain.COMBUSTAO, 10.5),
                VehicleModelInfo("Tracker", VehiclePowertrain.COMBUSTAO, 11.2),
                VehicleModelInfo("Cobalt", VehiclePowertrain.COMBUSTAO, 11.0),
                VehicleModelInfo("Cruze", VehiclePowertrain.COMBUSTAO, 10.5),
                VehicleModelInfo("Montana", VehiclePowertrain.COMBUSTAO, 11.5)
            )
        ),
        VehicleBrandInfo(
            brandName = "Fiat",
            models = listOf(
                VehicleModelInfo("Argo", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Mobi", VehiclePowertrain.COMBUSTAO, 13.0),
                VehicleModelInfo("Cronos", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Uno", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Siena / Grand Siena", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Pulse", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Fastback", VehiclePowertrain.COMBUSTAO, 11.0),
                VehicleModelInfo("Fiorino", VehiclePowertrain.COMBUSTAO, 11.0),
                VehicleModelInfo("Strada", VehiclePowertrain.COMBUSTAO, 12.0)
            )
        ),
        VehicleBrandInfo(
            brandName = "Volkswagen",
            models = listOf(
                VehicleModelInfo("Gol", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Voyage", VehiclePowertrain.COMBUSTAO, 12.0),
                VehicleModelInfo("Polo / Polo Track", VehiclePowertrain.COMBUSTAO, 13.0),
                VehicleModelInfo("Virtus", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("T-Cross", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Nivus", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Fox", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Up!", VehiclePowertrain.COMBUSTAO, 13.8)
            )
        ),
        VehicleBrandInfo(
            brandName = "Hyundai",
            models = listOf(
                VehicleModelInfo("HB20", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("HB20S (Sedan)", VehiclePowertrain.COMBUSTAO, 12.8),
                VehicleModelInfo("Creta", VehiclePowertrain.COMBUSTAO, 10.5)
            )
        ),
        VehicleBrandInfo(
            brandName = "Renault",
            models = listOf(
                VehicleModelInfo("Kwid", VehiclePowertrain.COMBUSTAO, 14.0),
                VehicleModelInfo("Sandero", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Logan", VehiclePowertrain.COMBUSTAO, 12.0),
                VehicleModelInfo("Stepway", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Kardian", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Duster", VehiclePowertrain.COMBUSTAO, 10.2),
                VehicleModelInfo("Kwid E-Tech", VehiclePowertrain.ELETRICO, 6.8),
                VehicleModelInfo("Megane E-Tech", VehiclePowertrain.ELETRICO, 6.0)
            )
        ),
        VehicleBrandInfo(
            brandName = "Toyota",
            models = listOf(
                VehicleModelInfo("Yaris Hatch", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Yaris Sedan", VehiclePowertrain.COMBUSTAO, 12.8),
                VehicleModelInfo("Etios Hatch", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Etios Sedan", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Corolla", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("Corolla Hybrid", VehiclePowertrain.HIBRIDO, 17.5),
                VehicleModelInfo("Corolla Cross", VehiclePowertrain.COMBUSTAO, 11.0),
                VehicleModelInfo("Corolla Cross Hybrid", VehiclePowertrain.HIBRIDO, 16.5),
                VehicleModelInfo("Prius", VehiclePowertrain.HIBRIDO, 18.0)
            )
        ),
        VehicleBrandInfo(
            brandName = "Honda",
            models = listOf(
                VehicleModelInfo("City Hatchback", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("City Sedan", VehiclePowertrain.COMBUSTAO, 12.8),
                VehicleModelInfo("Fit", VehiclePowertrain.COMBUSTAO, 12.0),
                VehicleModelInfo("Civic", VehiclePowertrain.COMBUSTAO, 11.0),
                VehicleModelInfo("Civic Hybrid", VehiclePowertrain.HIBRIDO, 18.0),
                VehicleModelInfo("HR-V", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("WR-V", VehiclePowertrain.COMBUSTAO, 11.5)
            )
        ),
        VehicleBrandInfo(
            brandName = "Nissan",
            models = listOf(
                VehicleModelInfo("Versa", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Kicks", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("March", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("Sentra", VehiclePowertrain.COMBUSTAO, 11.0)
            )
        ),
        VehicleBrandInfo(
            brandName = "BYD",
            models = listOf(
                VehicleModelInfo("Dolphin Mini", VehiclePowertrain.ELETRICO, 7.2),
                VehicleModelInfo("Dolphin", VehiclePowertrain.ELETRICO, 6.6),
                VehicleModelInfo("Song Plus DM-i", VehiclePowertrain.HIBRIDO_PLUGIN, 18.0, 5.5),
                VehicleModelInfo("King DM-i", VehiclePowertrain.HIBRIDO_PLUGIN, 20.0, 5.5),
                VehicleModelInfo("Yuan Plus", VehiclePowertrain.ELETRICO, 6.2),
                VehicleModelInfo("Seal", VehiclePowertrain.ELETRICO, 5.8)
            )
        ),
        VehicleBrandInfo(
            brandName = "GWM",
            models = listOf(
                VehicleModelInfo("Ora 03", VehiclePowertrain.ELETRICO, 6.5),
                VehicleModelInfo("Haval H6 HEV", VehiclePowertrain.HIBRIDO, 15.5),
                VehicleModelInfo("Haval H6 PHEV", VehiclePowertrain.HIBRIDO_PLUGIN, 18.0, 5.0)
            )
        ),
        VehicleBrandInfo(
            brandName = "Caoa Chery",
            models = listOf(
                VehicleModelInfo("Tiggo 5X", VehiclePowertrain.COMBUSTAO, 10.5),
                VehicleModelInfo("Tiggo 5X Pro Hybrid", VehiclePowertrain.HIBRIDO, 12.0),
                VehicleModelInfo("Tiggo 7 Pro Hybrid", VehiclePowertrain.HIBRIDO, 12.0),
                VehicleModelInfo("Arrizo 6 Pro Hybrid", VehiclePowertrain.HIBRIDO, 12.5),
                VehicleModelInfo("iCar", VehiclePowertrain.ELETRICO, 7.0),
                VehicleModelInfo("Tiggo 2", VehiclePowertrain.COMBUSTAO, 10.5)
            )
        ),
        VehicleBrandInfo(
            brandName = "Ford",
            models = listOf(
                VehicleModelInfo("Ka", VehiclePowertrain.COMBUSTAO, 13.0),
                VehicleModelInfo("Ka Sedan", VehiclePowertrain.COMBUSTAO, 13.0),
                VehicleModelInfo("EcoSport", VehiclePowertrain.COMBUSTAO, 10.5),
                VehicleModelInfo("Fiesta", VehiclePowertrain.COMBUSTAO, 11.5)
            )
        ),
        VehicleBrandInfo(
            brandName = "Peugeot",
            models = listOf(
                VehicleModelInfo("208", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("2008", VehiclePowertrain.COMBUSTAO, 11.2),
                VehicleModelInfo("e-208", VehiclePowertrain.ELETRICO, 6.2)
            )
        ),
        VehicleBrandInfo(
            brandName = "Citroën",
            models = listOf(
                VehicleModelInfo("C3", VehiclePowertrain.COMBUSTAO, 12.5),
                VehicleModelInfo("C3 Aircross", VehiclePowertrain.COMBUSTAO, 11.5),
                VehicleModelInfo("C4 Cactus", VehiclePowertrain.COMBUSTAO, 11.5)
            )
        ),
        VehicleBrandInfo(
            brandName = "Jeep",
            models = listOf(
                VehicleModelInfo("Renegade", VehiclePowertrain.COMBUSTAO, 10.0),
                VehicleModelInfo("Compass", VehiclePowertrain.COMBUSTAO, 10.0)
            )
        ),
        VehicleBrandInfo(
            brandName = "Outra Marca",
            models = listOf(
                VehicleModelInfo("Outro Modelo", VehiclePowertrain.COMBUSTAO, 11.5)
            )
        )
    )
}
