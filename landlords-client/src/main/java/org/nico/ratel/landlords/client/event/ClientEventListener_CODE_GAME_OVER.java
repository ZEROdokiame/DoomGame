package org.nico.ratel.landlords.client.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.nico.noson.Noson;
import org.nico.noson.entity.NoType;
import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.entity.Poker;
import org.nico.ratel.landlords.enums.ClientType;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_GAME_OVER extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		Map<String, Object> map = MapHelper.parser(data);
		String winnerTypeStr = map.get("winnerType").toString();
		String factionName;
		try{
			factionName = ClientType.valueOf(winnerTypeStr).zh();
		}catch(Exception ignore){
			factionName = winnerTypeStr;
		}

		SimplePrinter.printNotice("\n========== 对局结束 ==========");
		SimplePrinter.printNotice("获胜阵营：【" + factionName + "】");
		SimplePrinter.printNotice(map.get("winnerNickname") + " 出完了所有手牌！");

		if (map.containsKey("scores")){
			List<Map<String, Object>> scores = Noson.convert(map.get("scores"), new NoType<List<Map<String, Object>>>() {});

			// 判断玩家自己是否属于获胜阵营
			boolean myVictory = false;
			for (Map<String, Object> score : scores) {
				if (Objects.equals(score.get("clientId"), SimpleClient.id)) {
					int scoreInc = Integer.parseInt(score.get("scoreInc").toString());
					myVictory = scoreInc > 0;
					break;
				}
			}

			if (myVictory) {
				if (ClientType.LANDLORD.name().equals(winnerTypeStr)) {
					SimplePrinter.printNotice("你作为赖盖特之赐，独自击败了两名地狱幸存者！");
				} else {
					SimplePrinter.printNotice("你与同阵营的地狱幸存者携手获得了胜利！");
				}
			} else {
				if (ClientType.LANDLORD.name().equals(winnerTypeStr)) {
					SimplePrinter.printNotice("赖盖特之赐太过强大，你所在的地狱幸存者阵营战败了。");
				} else {
					SimplePrinter.printNotice("地狱幸存者联手将你击败，你作为赖盖特之赐陨落了。");
				}
			}

			// 展示其他玩家的剩余手牌
			for (Map<String, Object> score : scores) {
				if (! Objects.equals(score.get("clientId"), SimpleClient.id)) {
					SimplePrinter.printNotice(score.get("nickName").toString() + " 的剩余手牌：");
					SimplePrinter.printPokers(Noson.convert(score.get("pokers"), new NoType<List<Poker>>() {}));
				}
			}
			SimplePrinter.printNotice("");
			// 打印分数
			for (Map<String, Object> score : scores) {
				String scoreInc = score.get("scoreInc").toString();
				String scoreTotal = score.get("score").toString();
				if (SimpleClient.id != (int) score.get("clientId")) {
					SimplePrinter.printNotice(score.get("nickName").toString() + " 本局得分 " + scoreInc + "，总分 " + scoreTotal);
				} else {
					SimplePrinter.printNotice("你的本局得分 " + scoreInc + "，总分 " + scoreTotal);
				}
			}
			SimplePrinter.printNotice("==============================\n");
			ClientEventListener_CODE_GAME_READY.gameReady(channel);
		}
	}
}
