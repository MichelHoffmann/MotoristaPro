package com.example.model

data class Holiday(
    val day: Int,
    val month: Int, // 1-12
    val name: String,
    val description: String,
    val type: HolidayType
)

enum class HolidayType(val displayName: String) {
    NACIONAL("Feriado Nacional"),
    ESTADUAL_ACRE("Feriado Estadual (Acre)"),
    MUNICIPAL_RIO_BRANCO("Feriado Municipal (Rio Branco)"),
    FACULTATIVO("Ponto Facultativo")
}

object BrazilianHolidays {

    // Computes Easter Sunday for a given year using Anonymous Gregorian algorithm
    private fun getEasterSunday(year: Int): Pair<Int, Int> { // Pair(day, month 1-12)
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return Pair(day, month)
    }

    // Computes date offset in days from Easter
    private fun getDateOffsetFromEaster(year: Int, offsetDays: Int): Pair<Int, Int> {
        val (easterDay, easterMonth) = getEasterSunday(year)
        val cal = java.util.Calendar.getInstance()
        cal.set(year, easterMonth - 1, easterDay)
        cal.add(java.util.Calendar.DAY_OF_YEAR, offsetDays)
        return Pair(cal.get(java.util.Calendar.DAY_OF_MONTH), cal.get(java.util.Calendar.MONTH) + 1)
    }

    fun getHolidaysForYear(year: Int): List<Holiday> {
        val (carnavalDay, carnavalMonth) = getDateOffsetFromEaster(year, -47) // Terça-feira de Carnaval
        val (segundaCarnavalDay, segundaCarnavalMonth) = getDateOffsetFromEaster(year, -48) // Segunda de Carnaval
        val (cinzasDay, cinzasMonth) = getDateOffsetFromEaster(year, -46) // Quarta-feira de Cinzas
        val (sextaSantaDay, sextaSantaMonth) = getDateOffsetFromEaster(year, -2) // Sexta-feira Santa
        val (pascoaDay, pascoaMonth) = getDateOffsetFromEaster(year, 0) // Páscoa
        val (corpusChristiDay, corpusChristiMonth) = getDateOffsetFromEaster(year, 60) // Corpus Christi

        return listOf(
            // Feriados Nacionais Fixos
            Holiday(1, 1, "Confraternização Universal", "Ano Novo e Dia da Paz Mundial (Lei 10.607/2002)", HolidayType.NACIONAL),
            Holiday(21, 4, "Tiradentes", "Homenagem ao mártir da Inconfidência Mineira, Joaquim José da Silva Xavier", HolidayType.NACIONAL),
            Holiday(1, 5, "Dia do Trabalhador", "Celebração das conquistas dos trabalhadores e lutas trabalhistas", HolidayType.NACIONAL),
            Holiday(7, 9, "Independência do Brasil", "Comemoração da Proclamação da Independência em 1822 às margens do Ipiranga", HolidayType.NACIONAL),
            Holiday(12, 10, "Nossa Senhora Aparecida", "Padroeira do Brasil e comemoração do Dia das Crianças", HolidayType.NACIONAL),
            Holiday(2, 11, "Finados", "Dia de oração e lembrança dos entes queridos falecidos", HolidayType.NACIONAL),
            Holiday(15, 11, "Proclamação da República", "Marca a transição do Império para a República Federativa em 1889", HolidayType.NACIONAL),
            Holiday(20, 11, "Dia da Consciência Negra", "Homenagem a Zumbi dos Palmares e valorização da cultura afro-brasileira (Feriado Nacional Lei 14.759/2023)", HolidayType.NACIONAL),
            Holiday(25, 12, "Natal", "Celebração do nascimento de Jesus Cristo", HolidayType.NACIONAL),

            // Feriados Móveis Nacionais & Facultativos
            Holiday(sextaSantaDay, sextaSantaMonth, "Sexta-feira Santa", "Paixão de Cristo e reflexão religiosa cristã", HolidayType.NACIONAL),
            Holiday(pascoaDay, pascoaMonth, "Páscoa", "Domingo da Ressurreição de Jesus Cristo", HolidayType.NACIONAL),
            Holiday(segundaCarnavalDay, segundaCarnavalMonth, "Carnaval (Segunda-feira)", "Celebração carnavalesca tradicional", HolidayType.FACULTATIVO),
            Holiday(carnavalDay, carnavalMonth, "Carnaval (Terça-feira)", "Dia principal dos festejos de Carnaval", HolidayType.FACULTATIVO),
            Holiday(cinzasDay, cinzasMonth, "Quarta-feira de Cinzas", "Início do período da Quaresma (ponto facultativo até 14h)", HolidayType.FACULTATIVO),
            Holiday(corpusChristiDay, corpusChristiMonth, "Corpus Christi", "Solenidade católica do Santíssimo Sacramento", HolidayType.FACULTATIVO),
            Holiday(24, 12, "Véspera de Natal", "Ponto facultativo após as 14h", HolidayType.FACULTATIVO),
            Holiday(31, 12, "Véspera de Ano Novo", "Ponto facultativo para preparativos do réveillon", HolidayType.FACULTATIVO),
            Holiday(28, 10, "Dia do Servidor Público", "Celebração e valorização do funcionalismo público", HolidayType.FACULTATIVO),

            // Feriados Estaduais do Acre (Lei Estadual nº 14/1964 e nº 2.126/2009)
            Holiday(23, 1, "Dia do Evangélico", "Homenagem à comunidade evangélica acreana (Lei Estadual nº 1.538/2004)", HolidayType.ESTADUAL_ACRE),
            Holiday(8, 3, "Dia Internacional da Mulher", "Feriado Estadual no Acre celebrando as mulheres (Lei Estadual nº 1.411/2001)", HolidayType.ESTADUAL_ACRE),
            Holiday(15, 6, "Aniversário do Acre", "Elevação do Acre à categoria de Estado em 1962 (Lei Estadual nº 14/1964)", HolidayType.ESTADUAL_ACRE),
            Holiday(5, 9, "Dia da Amazônia", "Comemoração da criação da Província do Amazonas e conscientização ambiental no Acre", HolidayType.ESTADUAL_ACRE),
            Holiday(17, 11, "Tratado de Petrópolis", "Assinatura do tratado em 1903 que anexou o território do Acre ao Brasil", HolidayType.ESTADUAL_ACRE),

            // Feriados Municipais de Rio Branco - AC (Lei Municipal nº 1.399/2000)
            Holiday(20, 1, "São Sebastião", "Dia do padroeiro da Diocese e protetor da cidade", HolidayType.MUNICIPAL_RIO_BRANCO),
            Holiday(28, 12, "Aniversário de Rio Branco", "Fundação da cidade de Rio Branco, capital do Estado do Acre (1882)", HolidayType.MUNICIPAL_RIO_BRANCO)
        )
    }

    fun getHoliday(day: Int, month: Int, year: Int): Holiday? {
        val holidays = getHolidaysForYear(year)
        return holidays.find { it.day == day && it.month == month }
    }
}
