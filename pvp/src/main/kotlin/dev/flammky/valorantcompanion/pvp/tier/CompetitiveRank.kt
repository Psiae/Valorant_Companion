package dev.flammky.valorantcompanion.pvp.tier

sealed class CompetitiveRank() {

   object UNRANKED : CompetitiveRank() {}

   object IRON_1 : CompetitiveRank() {}

    object IRON_2 : CompetitiveRank() {}

    object IRON_3 : CompetitiveRank()  {}

    object BRONZE_1 : CompetitiveRank() {}

    object BRONZE_2 : CompetitiveRank() {}

    object BRONZE_3 : CompetitiveRank()  {}

    object SILVER_1 : CompetitiveRank() { }

    object SILVER_2 : CompetitiveRank() { }

    object SILVER_3 : CompetitiveRank() {}

    object GOLD_1 : CompetitiveRank() {}

    object GOLD_2 : CompetitiveRank() {}

    object GOLD_3 : CompetitiveRank() {}

    object PLATINUM_1 : CompetitiveRank() {}

    object PLATINUM_2 : CompetitiveRank() {}

    object PLATINUM_3 : CompetitiveRank() {}

    object DIAMOND_1 : CompetitiveRank() { }

    object DIAMOND_2 : CompetitiveRank() {}

    object DIAMOND_3 : CompetitiveRank() {}

    // ASCENDANT was introduced on EP5A1

    object ASCENDANT_1 : CompetitiveRank() {}

    object ASCENDANT_2 : CompetitiveRank() {}

    object ASCENDANT_3 : CompetitiveRank() {}


    object IMMORTAL_1 : CompetitiveRank() {}

    object IMMORTAL_2 : CompetitiveRank() {}

    object IMMORTAL_3 : CompetitiveRank() {}

    // Immortal rank were merged from EP2A1 until exclusive EP3A2

    object IMMORTAL_MERGED : CompetitiveRank() {}

    object RADIANT : CompetitiveRank() {}
}