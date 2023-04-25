package dev.flammky.valorantcompanion.pvp.party

data class PartyInviteRequest(
    val id: String,
    val partyId: String,
    val requestedBySubject: String,
    val subjects: List<String>,
    // ISO 8601
    val createdAt: String,
    // ISO 8601
    val refreshedAt: String,
    val expiresIn: Int
) {
}