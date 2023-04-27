package dev.flammky.valorantcompanion.auth.riot.region

import dev.flammky.valorantcompanion.auth.riot.region.ISO_3166_Country.Companion.register

sealed class RiotRegion(
    val assignedUrlName: String
) {

    object NA : RiotRegion("na")

    object LATAM : RiotRegion("latam")

    object BR : RiotRegion("br")

    object EU : RiotRegion("eu")

    object APAC : RiotRegion("ap")

    object KR : RiotRegion("kr")

    companion object {
        fun resolveByRegionName(str: String): RiotRegion? {
            return when (str.lowercase()) {
                "latam", "latin america", "south america", "central america", "caribbean" -> LATAM
                "br", "bra", "brazil" -> BR
                "na", "north america" -> NA
                "eu", "europe", "emea" -> EU
                "ap", "apac", "asia pacific" -> APAC
                "kr", "kor", "korea" -> KR
                else -> null
            }
        }

        fun resolveByCountryCode(code: String): RiotRegion? {
            return when (code.length) {
                2 -> {
                    if (code.all(Char::isLetter)) {
                        resolveAlpha2CountryCode(code)
                    } else {
                        throw IllegalArgumentException("Country Code with size of 2 can only be consisted of Letters, code=$code")
                    }
                }
                3 -> {
                    if (code.all(Char::isLetter)) {
                        resolveAlpha3CountryCode(code)
                    }  else if (code.all(Char::isLetter)) {
                        resolveNumericCountryCode(code)
                    }  else {
                        throw IllegalArgumentException("Country Code with size of 3 can only either be all Letters or all Numbers, code=$code")
                    }
                }
                else -> {
                    throw IllegalArgumentException(
                        "only Alpha-2, Alpha-3, and Numeric country code are resolve-able, code=$code"
                    )
                }
            }
        }

        private fun resolveAlpha2CountryCode(code: String): RiotRegion? {
            if (code.length != 2 || !code.all(Char::isLetter)) {
                throw IllegalArgumentException("Alpha2CountryCode must consist of only 2 letter, code=$code")
            }
            val uc_code =
                code.uppercase()
            // Brazil
            if (uc_code == "BR") {
                return BR
            }
            // South Korea
            if (uc_code == "KR") {
                return KR
            }
            // North America
            if (ISO_3166_Northern_America_Countries.any { it.alpha2 == uc_code }) {
                return NA
            }
            // Latin America
            if (ISO_3166_Latin_America_Countries.any { it.alpha2 == uc_code }) {
                return LATAM
            }
            // EMEA
            if (
                ISO_3166_Eastern_Europe_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_Northern_Europe_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_Southern_Europe_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_Western_Europe_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_Africa_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_Middle_East_Countries.any { it.alpha2 == uc_code }
            ) {
                return EU
            }
            // APAC
            if (
                ISO_3166_Eastern_Asia_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_South_Asia_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_SouthEast_Asia_Countries.any { it.alpha2 == uc_code } ||
                ISO_3166_Oceania_Countries.any { it.alpha2 == uc_code }
            ) {
                return APAC
            }
            return null
        }

        private fun resolveAlpha3CountryCode(code: String): RiotRegion? {
            if (code.length != 3 || !code.all(Char::isLetter)) {
                throw IllegalArgumentException("Alpha3CountryCode must consist of only 3 letter, code=$code")
            }
            val uc_code =
                code.uppercase()
            // Brazil
            if (uc_code == "BRA") {
                return BR
            }
            // South Korea
            if (uc_code == "KOR") {
                return KR
            }
            // North America
            if (ISO_3166_Northern_America_Countries.any { it.alpha3 == uc_code }) {
                return NA
            }
            // Latin America
            if (ISO_3166_Latin_America_Countries.any { it.alpha3 == uc_code }) {
                return LATAM
            }
            // EMEA
            if (
                ISO_3166_Eastern_Europe_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_Northern_Europe_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_Southern_Europe_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_Western_Europe_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_Africa_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_Middle_East_Countries.any { it.alpha3 == uc_code }
            ) {
                return EU
            }
            // APAC
            if (
                ISO_3166_Eastern_Asia_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_South_Asia_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_SouthEast_Asia_Countries.any { it.alpha3 == uc_code } ||
                ISO_3166_Oceania_Countries.any { it.alpha3 == uc_code }
            ) {
                return APAC
            }
            return null
        }

        private fun resolveNumericCountryCode(code: String): RiotRegion? {
            if (code.length != 3 || !code.all(Char::isDigit)) {
                throw IllegalArgumentException("NumericCountryCode must consist of only 3 digit, code=$code")
            }
            // Brazil
            if (code == "076") {
                return BR
            }
            // South Korea
            if (code == "410") {
                return KR
            }
            // North America
            if (ISO_3166_Northern_America_Countries.any { it.numeric == code }) {
                return NA
            }
            // Latin America
            if (ISO_3166_Latin_America_Countries.any { it.numeric == code }) {
                return LATAM
            }
            // EMEA
            if (
                ISO_3166_Eastern_Europe_Countries.any { it.numeric == code } ||
                ISO_3166_Northern_Europe_Countries.any { it.numeric == code } ||
                ISO_3166_Southern_Europe_Countries.any { it.numeric == code } ||
                ISO_3166_Western_Europe_Countries.any { it.numeric == code } ||
                ISO_3166_Africa_Countries.any { it.numeric == code } ||
                ISO_3166_Middle_East_Countries.any { it.numeric == code }
            ) {
                return EU
            }
            // APAC
            if (
                ISO_3166_Eastern_Asia_Countries.any { it.numeric == code } ||
                ISO_3166_South_Asia_Countries.any { it.numeric == code } ||
                ISO_3166_SouthEast_Asia_Countries.any { it.numeric == code } ||
                ISO_3166_Oceania_Countries.any { it.numeric == code }
            ) {
                return APAC
            }
            return null
        }
    }
}

private data class ISO_3166_Country(
    val name: String,
    val alpha2: String,
    val alpha3: String,
    val numeric: String
) {
    init {
        check(alpha2.length == 2 && alpha2.all(Char::isLetter))
        check(alpha3.length == 3 && alpha3.all(Char::isLetter))
        check(numeric.length == 3 && numeric.all(Char::isDigit))
    }
    companion object {
        private val NAME_SLOT = mutableSetOf<String>()
        private val ALPHA_2_SLOT = mutableSetOf<String>()
        private val ALPHA_3_SLOT = mutableSetOf<String>()
        private val NUMERIC_SLOT = mutableSetOf<String>()

        fun ISO_3166_Country.register() {
            synchronized(NAME_SLOT) {
                check(NAME_SLOT.add(name))
                check(ALPHA_2_SLOT.add(alpha2))
                check(ALPHA_3_SLOT.add(alpha3))
                check(NUMERIC_SLOT.add(numeric))
            }
        }
    }
}

private val ISO_3166_Africa_Countries = listOf(
    ISO_3166_Country("Algeria", "DZ", "DZA", "012"),
    ISO_3166_Country("Angola", "AO", "AGO", "024"),
    ISO_3166_Country("Benin", "BJ", "BEN", "204"),
    ISO_3166_Country("Botswana", "BW", "BWA", "072"),
    ISO_3166_Country("Burkina Faso", "BF", "BFA", "854"),
    ISO_3166_Country("Burundi", "BI", "BDI", "108"),
    ISO_3166_Country("Cabo Verde", "CV", "CPV", "132"),
    ISO_3166_Country("Cameroon", "CM", "CMR", "120"),
    ISO_3166_Country("Central African Republic", "CF", "CAF", "140"),
    ISO_3166_Country("Chad", "TD", "TCD", "148"),
    ISO_3166_Country("Comoros", "KM", "COM", "174"),
    ISO_3166_Country("Congo", "CG", "COG", "178"),
    ISO_3166_Country("Cote d'Ivoire", "CI", "CIV", "384"),
    ISO_3166_Country("Djibouti", "DJ", "DJI", "262"),
    ISO_3166_Country("Egypt", "EG", "EGY", "818"),
    ISO_3166_Country("Equatorial Guinea", "GQ", "GNQ", "226"),
    ISO_3166_Country("Eritrea", "ER", "ERI", "232"),
    ISO_3166_Country("Eswatini (Swaziland)", "SZ", "SWZ", "748"),
    ISO_3166_Country("Ethiopia", "ET", "ETH", "231"),
    ISO_3166_Country("Gabon", "GA", "GAB", "266"),
    ISO_3166_Country("Gambia", "GM", "GMB", "270"),
    ISO_3166_Country("Ghana", "GH", "GHA", "288"),
    ISO_3166_Country("Guinea", "GN", "GIN", "324"),
    ISO_3166_Country("Guinea-Bissau", "GW", "GNB", "624"),
    ISO_3166_Country("Kenya", "KE", "KEN", "404"),
    ISO_3166_Country("Lesotho", "LS", "LSO", "426"),
    ISO_3166_Country("Liberia", "LR", "LBR", "430"),
    ISO_3166_Country("Libya", "LY", "LBY", "434"),
    ISO_3166_Country("Madagascar", "MG", "MDG", "450"),
    ISO_3166_Country("Malawi", "MW", "MWI", "454"),
    ISO_3166_Country("Mali", "ML", "MLI", "466"),
    ISO_3166_Country("Mauritania", "MR", "MRT", "478"),
    ISO_3166_Country("Mauritius", "MU", "MUS", "480"),
    ISO_3166_Country("Mayotte", "YT", "MYT", "175"),
    ISO_3166_Country("Morocco", "MA", "MAR", "504"),
    ISO_3166_Country("Mozambique", "MZ", "MOZ", "508"),
    ISO_3166_Country("Namibia", "NA", "NAM", "516"),
    ISO_3166_Country("Niger", "NE", "NER", "562"),
    ISO_3166_Country("Nigeria", "NG", "NGA", "566"),
    ISO_3166_Country("Réunion", "RE", "REU", "638"),
    ISO_3166_Country("Rwanda", "RW", "RWA", "646"),
    ISO_3166_Country("Saint Helena, Ascension and Tristan da Cunha", "SH", "SHN", "654"),
    ISO_3166_Country("Sao Tome and Principe", "ST", "STP", "678"),
    ISO_3166_Country("Senegal", "SN", "SEN", "686"),
    ISO_3166_Country("Seychelles", "SC", "SYC", "690"),
    ISO_3166_Country("Sierra Leone", "SL", "SLE", "694"),
    ISO_3166_Country("Somalia", "SO", "SOM", "706"),
    ISO_3166_Country("South Africa", "ZA", "ZAF", "710"),
    ISO_3166_Country("South Sudan", "SS", "SSD", "728"),
    ISO_3166_Country("Sudan", "SD", "SDN", "729"),
    ISO_3166_Country("Tanzania, United Republic of", "TZ", "TZA", "834"),
    ISO_3166_Country("Togo", "TG", "TGO", "768"),
    ISO_3166_Country("Tunisia", "TN", "TUN", "788"),
    ISO_3166_Country("Uganda", "UG", "UGA", "800"),
    ISO_3166_Country("Western Sahara", "EH", "ESH", "732"),
    ISO_3166_Country("Zambia", "ZM", "ZMB", "894"),
    ISO_3166_Country("Zimbabwe", "ZW", "ZWE", "716"),
).onEach { it.register() }

private val ISO_3166_Central_America_Countries = listOf(
    ISO_3166_Country("Belize", "BZ", "BLZ", "084"),
    ISO_3166_Country("Costa Rica", "CR", "CRI", "188"),
    ISO_3166_Country("El Salvador", "SV", "SLV", "222"),
    ISO_3166_Country("Guatemala", "GT", "GTM", "320"),
    ISO_3166_Country("Honduras", "HN", "HND", "340"),
    ISO_3166_Country("Nicaragua", "NI", "NIC", "558"),
    ISO_3166_Country("Panama", "PA", "PAN", "591")
).onEach { it.register() }

private val ISO_3166_Caribbean_Countries = listOf(
    ISO_3166_Country("Anguilla", "AI", "AIA", "660"),
    ISO_3166_Country("Antigua and Barbuda", "AG", "ATG", "028"),
    ISO_3166_Country("Aruba", "AW", "ABW", "533"),
    ISO_3166_Country("Bahamas", "BS", "BHS", "044"),
    ISO_3166_Country("Barbados", "BB", "BRB", "052"),
    ISO_3166_Country("Bonaire, Sint Eustatius and Saba", "BQ", "BES", "535"),
    ISO_3166_Country("British Virgin Islands", "VG", "VGB", "092"),
    ISO_3166_Country("Cayman Islands", "KY", "CYM", "136"),
    ISO_3166_Country("Cuba", "CU", "CUB", "192"),
    ISO_3166_Country("Curaçao", "CW", "CUW", "531"),
    ISO_3166_Country("Dominica", "DM", "DMA", "212"),
    ISO_3166_Country("Dominican Republic", "DO", "DOM", "214"),
    ISO_3166_Country("Grenada", "GD", "GRD", "308"),
    ISO_3166_Country("Guadeloupe", "GP", "GLP", "312"),
    ISO_3166_Country("Haiti", "HT", "HTI", "332"),
    ISO_3166_Country("Jamaica", "JM", "JAM", "388"),
    ISO_3166_Country("Martinique", "MQ", "MTQ", "474"),
    ISO_3166_Country("Montserrat", "MS", "MSR", "500"),
    ISO_3166_Country("Puerto Rico", "PR", "PRI", "630"),
    ISO_3166_Country("Saint Barthélemy", "BL", "BLM", "652"),
    ISO_3166_Country("Saint Kitts and Nevis", "KN", "KNA", "659"),
    ISO_3166_Country("Saint Lucia", "LC", "LCA", "662"),
    ISO_3166_Country("Saint Martin (French part)", "MF", "MAF", "663"),
    ISO_3166_Country("Saint Vincent and the Grenadines", "VC", "VCT", "670"),
    ISO_3166_Country("Sint Maarten (Dutch part)", "SX", "SXM", "534"),
    ISO_3166_Country("Trinidad and Tobago", "TT", "TTO", "780"),
    ISO_3166_Country("Turks and Caicos Islands", "TC", "TCA", "796"),
    ISO_3166_Country("United States Virgin Islands", "VI", "VIR", "850")
).onEach { it.register() }

private val ISO_3166_Latin_America_Countries = listOf(
    ISO_3166_Country("Mexico", "MX", "MEX", "484"),
    ISO_3166_Country("Argentina", "AR", "ARG", "032"),
    ISO_3166_Country("Bolivia (Plurinational State of)", "BO", "BOL", "068"),
    ISO_3166_Country("Brazil", "BR", "BRA", "076"),
    ISO_3166_Country("Chile", "CL", "CHL", "152"),
    ISO_3166_Country("Colombia", "CO", "COL", "170"),
    ISO_3166_Country("Ecuador", "EC", "ECU", "218"),
    ISO_3166_Country("Falkland Islands (Malvinas)", "FK", "FLK", "238"),
    ISO_3166_Country("French Guiana", "GF", "GUF", "254"),
    ISO_3166_Country("Guyana", "GY", "GUY", "328"),
    ISO_3166_Country("Paraguay", "PY", "PRY", "600"),
    ISO_3166_Country("Peru", "PE", "PER", "604"),
    ISO_3166_Country("South Georgia and the South Sandwich Islands", "GS", "SGS", "239"),
    ISO_3166_Country("Suriname", "SR", "SUR", "740"),
    ISO_3166_Country("Uruguay", "UY", "URY", "858"),
    ISO_3166_Country("Venezuela (Bolivarian Republic of)", "VE", "VEN", "862")
).onEach { it.register() }.run {
    listOf(this, ISO_3166_Central_America_Countries, ISO_3166_Caribbean_Countries).flatten()
}

// Northern as opposed to North because Mexico is within the LATAM region
private val ISO_3166_Northern_America_Countries = listOf(
    ISO_3166_Country("Bermuda", "BM", "BMU", "060"),
    ISO_3166_Country("Canada", "CA", "CAN", "124"),
    ISO_3166_Country("Greenland", "GL", "GRL", "304"),
    ISO_3166_Country("Saint Pierre and Miquelon", "PM", "SPM", "666"),
    ISO_3166_Country("United States of America", "US", "USA", "840"),
).onEach { it.register() }

private val ISO_3166_Eastern_Europe_Countries = listOf(
    ISO_3166_Country("Belarus", "BY", "BLR", "112"),
    ISO_3166_Country("Bulgaria", "BG", "BGR", "100"),
    ISO_3166_Country("Czech Republic", "CZ", "CZE", "203"),
    ISO_3166_Country("Hungary", "HU", "HUN", "348"),
    ISO_3166_Country("Poland", "PL", "POL", "616"),
    ISO_3166_Country("Moldova, Republic of", "MD", "MDA", "498"),
    ISO_3166_Country("Romania", "RO", "ROU", "642"),
    ISO_3166_Country("Russian Federation", "RU", "RUS", "643"),
    ISO_3166_Country("Slovakia", "SK", "SVK", "703"),
    ISO_3166_Country("Ukraine", "UA", "UKR", "804")
).onEach { it.register() }

private val ISO_3166_Northern_Europe_Countries = listOf(
    ISO_3166_Country("Åland Islands", "AX", "ALA", "248"),
    ISO_3166_Country("Denmark", "DK", "DNK", "208"),
    ISO_3166_Country("Estonia", "EE", "EST", "233"),
    ISO_3166_Country("Faroe Islands", "FO", "FRO", "234"),
    ISO_3166_Country("Finland", "FI", "FIN", "246"),
    ISO_3166_Country("Guernsey", "GG", "GGY", "831"),
    ISO_3166_Country("Iceland", "IS", "ISL", "352"),
    ISO_3166_Country("Ireland", "IE", "IRL", "372"),
    ISO_3166_Country("Isle of Man", "IM", "IMN", "833"),
    ISO_3166_Country("Jersey", "JE", "JEY", "832"),
    ISO_3166_Country("Latvia", "LV", "LVA", "428"),
    ISO_3166_Country("Lithuania", "LT", "LTU", "440"),
    ISO_3166_Country("Norway", "NO", "NOR", "578"),
    ISO_3166_Country("Svalbard and Jan Mayen", "SJ", "SJM", "744"),
    ISO_3166_Country("Sweden", "SE", "SWE", "752"),
    ISO_3166_Country("United Kingdom", "GB", "GBR", "826")
).onEach { it.register() }

private val ISO_3166_Southern_Europe_Countries = listOf(
    ISO_3166_Country("Albania", "AL", "ALB", "008"),
    ISO_3166_Country("Andorra", "AD", "AND", "020"),
    ISO_3166_Country("Bosnia and Herzegovina", "BA", "BIH", "070"),
    ISO_3166_Country("Croatia", "HR", "HRV", "191"),
    ISO_3166_Country("Gibraltar", "GI", "GIB", "292"),
    ISO_3166_Country("Greece", "GR", "GRC", "300"),
    ISO_3166_Country("Holy See (Vatican City State)", "VA", "VAT", "336"),
    ISO_3166_Country("Italy", "IT", "ITA", "380"),
    ISO_3166_Country("Kosovo, Republic of", "XK", "XKX", "383"),
    ISO_3166_Country("Malta", "MT", "MLT", "470"),
    ISO_3166_Country("Montenegro", "ME", "MNE", "499"),
    ISO_3166_Country("North Macedonia", "MK", "MKD", "807"),
    ISO_3166_Country("Portugal", "PT", "PRT", "620"),
    ISO_3166_Country("San Marino", "SM", "SMR", "674"),
    ISO_3166_Country("Serbia", "RS", "SRB", "688"),
    ISO_3166_Country("Slovenia", "SI", "SVN", "705"),
    ISO_3166_Country("Spain", "ES", "ESP", "724")
).onEach { it.register() }

private val ISO_3166_Western_Europe_Countries = listOf(
    ISO_3166_Country("Austria", "AT", "AUT", "040"),
    ISO_3166_Country("Belgium", "BE", "BEL", "056"),
    ISO_3166_Country("France", "FR", "FRA", "250"),
    ISO_3166_Country("Germany", "DE", "DEU", "276"),
    ISO_3166_Country("Liechtenstein", "LI", "LIE", "438"),
    ISO_3166_Country("Luxembourg", "LU", "LUX", "442"),
    ISO_3166_Country("Monaco", "MC", "MCO", "492"),
    ISO_3166_Country("Netherlands", "NL", "NLD", "528"),
    ISO_3166_Country("Switzerland", "CH", "CHE", "756")
).onEach { it.register() }

private val ISO_3166_Middle_East_Countries = listOf(
    ISO_3166_Country("Bahrain", "BH", "BHR", "048"),
    ISO_3166_Country("Cyprus", "CY", "CYP", "196"),
    ISO_3166_Country("Egypt", "EG", "EGY", "818"),
    ISO_3166_Country("Iran, Islamic Republic of", "IR", "IRN", "364"),
    ISO_3166_Country("Iraq", "IQ", "IRQ", "368"),
    ISO_3166_Country("Israel", "IL", "ISR", "376"),
    ISO_3166_Country("Jordan", "JO", "JOR", "400"),
    ISO_3166_Country("Kuwait", "KW", "KWT", "414"),
    ISO_3166_Country("Lebanon", "LB", "LBN", "422"),
    ISO_3166_Country("Oman", "OM", "OMN", "512"),
    ISO_3166_Country("Palestine, State of", "PS", "PSE", "275"),
    ISO_3166_Country("Qatar", "QA", "QAT", "634"),
    ISO_3166_Country("Saudi Arabia", "SA", "SAU", "682"),
    ISO_3166_Country("Syrian Arab Republic", "SY", "SYR", "760"),
    ISO_3166_Country("Turkey", "TR", "TUR", "792"),
    ISO_3166_Country("United Arab Emirates", "AE", "ARE", "784"),
    ISO_3166_Country("Yemen", "YE", "YEM", "887")
).onEach { it.register() }

private val ISO_3166_Eastern_Asia_Countries = listOf(
    ISO_3166_Country("China", "CN", "CHN", "156"),
    ISO_3166_Country("Hong Kong", "HK", "HKG", "344"),
    ISO_3166_Country("Japan", "JP", "JPN", "392"),
    ISO_3166_Country("North Korea", "KP", "PRK", "408"),
    ISO_3166_Country("South Korea", "KR", "KOR", "410"),
    ISO_3166_Country("Macao", "MO", "MAC", "446"),
    ISO_3166_Country("Mongolia", "MN", "MNG", "496"),
    ISO_3166_Country("Taiwan, Province of China", "TW", "TWN", "158")
).onEach { it.register() }

private val ISO_3166_South_Asia_Countries = listOf(
    ISO_3166_Country("Afghanistan", "AF", "AFG", "004"),
    ISO_3166_Country("Bangladesh", "BD", "BGD", "050"),
    ISO_3166_Country("Bhutan", "BT", "BTN", "064"),
    ISO_3166_Country("India", "IN", "IND", "356"),
    ISO_3166_Country("Maldives", "MV", "MDV", "462"),
    ISO_3166_Country("Nepal", "NP", "NPL", "524"),
    ISO_3166_Country("Pakistan", "PK", "PAK", "586"),
    ISO_3166_Country("Sri Lanka", "LK", "LKA", "144")
).onEach { it.register() }

private val ISO_3166_SouthEast_Asia_Countries = listOf(
    ISO_3166_Country("Brunei Darussalam", "BN", "BRN", "096"),
    ISO_3166_Country("Cambodia", "KH", "KHM", "116"),
    ISO_3166_Country("Indonesia", "ID", "IDN", "360"),
    ISO_3166_Country("Lao People's Democratic Republic", "LA", "LAO", "418"),
    ISO_3166_Country("Malaysia", "MY", "MYS", "458"),
    ISO_3166_Country("Myanmar", "MM", "MMR", "104"),
    ISO_3166_Country("Philippines", "PH", "PHL", "608"),
    ISO_3166_Country("Singapore", "SG", "SGP", "702"),
    ISO_3166_Country("Thailand", "TH", "THA", "764"),
    ISO_3166_Country("Timor-Leste", "TL", "TLS", "626"),
    ISO_3166_Country("Viet Nam", "VN", "VNM", "704")
).onEach { it.register() }

private val ISO_3166_Oceania_Countries = listOf(
    ISO_3166_Country("Australia", "AU", "AUS", "036"),
    ISO_3166_Country("Fiji", "FJ", "FJI", "242"),
    ISO_3166_Country("Kiribati", "KI", "KIR", "296"),
    ISO_3166_Country("Marshall Islands", "MH", "MHL", "584"),
    ISO_3166_Country("Micronesia (Federated States of)", "FM", "FSM", "583"),
    ISO_3166_Country("Nauru", "NR", "NRU", "520"),
    ISO_3166_Country("New Zealand", "NZ", "NZL", "554"),
    ISO_3166_Country("Palau", "PW", "PLW", "585"),
    ISO_3166_Country("Papua New Guinea", "PG", "PNG", "598"),
    ISO_3166_Country("Samoa", "WS", "WSM", "882"),
    ISO_3166_Country("Solomon Islands", "SB", "SLB", "090"),
    ISO_3166_Country("Tonga", "TO", "TON", "776"),
    ISO_3166_Country("Tuvalu", "TV", "TUV", "798"),
    ISO_3166_Country("Vanuatu", "VU", "VUT", "548")
).onEach { it.register() }