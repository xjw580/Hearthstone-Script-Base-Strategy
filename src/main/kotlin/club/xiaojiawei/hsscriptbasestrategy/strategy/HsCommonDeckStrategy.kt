package club.xiaojiawei.hsscriptbasestrategy.strategy

import club.xiaojiawei.hsscriptstrategysdk.DeckStrategy
import club.xiaojiawei.hsscriptcardsdk.bean.Card
import club.xiaojiawei.hsscriptcardsdk.data.BaseData
import club.xiaojiawei.hsscriptcardsdk.data.CARD_INFO_TRIE
import club.xiaojiawei.hsscriptbase.enums.RunModeEnum
import club.xiaojiawei.hsscriptcardsdk.status.WAR
import club.xiaojiawei.hsscriptbasestrategy.util.DeckStrategyUtil

/**
 * @author 肖嘉威
 * @date 2024/9/8 14:56
 */
class HsCommonDeckStrategy : DeckStrategy() {
    override fun name(): String = "基础策略"

    override fun description(): String = "未对卡牌和卡组适配，自行组一套无战吼无法术的套牌即可"

    override fun getRunMode(): Array<RunModeEnum> =
        arrayOf(RunModeEnum.CASUAL, RunModeEnum.STANDARD, RunModeEnum.WILD, RunModeEnum.PRACTICE)

    override fun deckCode(): String = ""

    override fun id(): String = "e71234fa-0-base-deck-97e9-1f4e126cd33b"

    override fun referWeight(): Boolean = true

    override fun referPowerWeight(): Boolean = true

    override fun referChangeWeight(): Boolean = true

    override fun referCardInfo(): Boolean = true

    override fun executeChangeCard(cards: HashSet<Card>) {
        if (BaseData.enableChangeWeight) {
            val weightCards = DeckStrategyUtil.convertToSimulateCard(cards.toList())
            for (card in weightCards) {
                if (card.changeWeight < 0) {
                    cards.remove(card.card)
                }
            }
        } else {
            cards.removeIf { card -> card.cost > 2 }
        }
    }

    override fun executeOutCard() {
        val me = WAR.me
        val rival = WAR.rival

        DeckStrategyUtil.powerCard(me, rival)

        DeckStrategyUtil.cleanPlay()

        DeckStrategyUtil.powerCard(me, rival)

//        使用技能
        me.playArea.power?.let { powerCard ->
            if (me.usableResource >= powerCard.cost || powerCard.cost == 0) {
                CARD_INFO_TRIE[powerCard.cardId]?.let { cardInfo ->
                    cardInfo.powerActions.firstOrNull()?.powerExec(powerCard, cardInfo.effectType, WAR)
                } ?: let {
                    powerCard.action.power()
                }
            }
        }
        DeckStrategyUtil.cleanPlay()

        me.playArea.cards.toList().forEach { card: Card ->
            if (card.isLaunchpad && me.usableResource >= card.launchCost()) {
                card.action.launch()
            }
        }
    }

    override fun executeDiscoverChooseCard(vararg cards: Card): Int = 1
}
