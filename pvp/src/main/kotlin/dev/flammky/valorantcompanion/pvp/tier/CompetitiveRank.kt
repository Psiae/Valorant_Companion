package dev.flammky.valorantcompanion.pvp.tier

sealed class CompetitiveRank(
 val displayname: String,
) {

 object UNRANKED : CompetitiveRank(
  displayname = "unranked"
 ) {}

 object UNUSED_1 : CompetitiveRank(
  displayname = "unused_1"
 ) {}

 object UNUSED_2 : CompetitiveRank(
  displayname = "unused_2"
 ) {}

 object IRON_1 : CompetitiveRank(
  displayname = "iron 1"
 ) {}

 object IRON_2 : CompetitiveRank(
  displayname = "iron 2"
 ) {}

 object IRON_3 : CompetitiveRank(
  displayname = "iron 3"
 )  {}

 object BRONZE_1 : CompetitiveRank(
  displayname = "bronze 1"
 ) {}

 object BRONZE_2 : CompetitiveRank(
  displayname = "bronze 2"
 ) {}

 object BRONZE_3 : CompetitiveRank(
  displayname = "bronze 3"
 )  {}

 object SILVER_1 : CompetitiveRank(
  displayname = "silver 1"
 ) { }

 object SILVER_2 : CompetitiveRank(
  displayname = "silver 2"
 ) { }

 object SILVER_3 : CompetitiveRank(
  displayname = "silver 3"
 ) {}

 object GOLD_1 : CompetitiveRank(
  displayname = "gold 1"
 ) {}

 object GOLD_2 : CompetitiveRank(
  displayname = "gold 2"
 ) {}

 object GOLD_3 : CompetitiveRank(
  displayname = "gold 3"
 ) {}

 object PLATINUM_1 : CompetitiveRank(
  displayname = "platinum 1"
 ) {}

 object PLATINUM_2 : CompetitiveRank(
  displayname = "platinum 2"
 ) {}

 object PLATINUM_3 : CompetitiveRank(
  displayname = "platinum 3"
 ) {}

 object DIAMOND_1 : CompetitiveRank(
  displayname = "diamond 1"
 ) { }

 object DIAMOND_2 : CompetitiveRank(
  displayname = "diamond 2"
 ) {}

 object DIAMOND_3 : CompetitiveRank(
  displayname = "diamond 3"
 ) {}

 // ASCENDANT was introduced on EP5A1

 object ASCENDANT_1 : CompetitiveRank(
  displayname = "ascendant 1"
 ) {}

 object ASCENDANT_2 : CompetitiveRank(
  displayname = "ascendant 2"
 ) {}

 object ASCENDANT_3 : CompetitiveRank(
  displayname = "ascendant 3"
 ) {}


 object IMMORTAL_1 : CompetitiveRank(
  displayname = "immortal 1"
 ) {}

 object IMMORTAL_2 : CompetitiveRank(
  displayname = "immortal 2"
 ) {}

 object IMMORTAL_3 : CompetitiveRank(
  displayname = "immortal 3"
 ) {}

 // Immortal rank were merged from EP2A1 until exclusive EP3A2

 object IMMORTAL_MERGED : CompetitiveRank(
  displayname = "immortal"
 ) {}

 object RADIANT : CompetitiveRank(
  displayname = "radiant"
 ) {}

 companion object {

 }
}