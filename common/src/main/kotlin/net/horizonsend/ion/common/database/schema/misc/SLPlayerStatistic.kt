package net.horizonsend.ion.common.database.schema.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.DbObjectCompanion
import org.litote.kmongo.inc
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import org.litote.kmongo.upsert
import kotlin.reflect.KProperty

data class SLPlayerStatistic(
	override val _id: SLPlayerId,
	// If you add any values to the collection after you have created the document, they need to have a default value, else you will get errors
	val aiShipsKilled: Int = 0,
	val bazaarProfit: Int = 0,

	val factionKills: Map<String, Int> = mapOf()
) : DbObject {
	companion object : DbObjectCompanion<SLPlayer, SLPlayerId>(SLPlayer::class, setup = {}) {
		// Can put functions here for whatever
		fun <T> setStatistic(id: SLPlayerId, field: KProperty<T?>, value: T?) {
			SLPlayerStatistic.col.updateOneById(id, setValue(field, value), upsert())
		}
		fun <T: Number> incStatistic(id: SLPlayerId, field: KProperty<T>, value: T) {
			SLPlayerStatistic.col.updateOneById(id, inc(field, value), upsert())
		}
		fun getAIShipsKilled(id: SLPlayerId): Int = findPropById(id, SLPlayerStatistic::aiShipsKilled) ?: 0

		fun getBazaarProfit(id: SLPlayerId): Int = findPropById(id, SLPlayerStatistic::bazaarProfit) ?: 0

		fun incrementFactionKIll(player: SLPlayerId, factionIdentifier: String): Map<String, Int> {
			val amount = SLPlayerStatistic.findPropById(player, SLPlayerStatistic::factionKills)?.toMutableMap()
			val new = amount?.let {
				it[factionIdentifier] = (it[factionIdentifier] ?: 0) + 1
				it
			} ?: mutableMapOf(factionIdentifier to 1)

			SLPlayerStatistic.col.updateOneById(player, setValue(SLPlayerStatistic::factionKills, new.toMap()), upsert())

			return new
		}
	}
}
