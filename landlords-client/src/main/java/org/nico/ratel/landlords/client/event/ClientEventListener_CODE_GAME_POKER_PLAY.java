package org.nico.ratel.landlords.client.event;

import io.netty.channel.Channel;
import org.nico.noson.Noson;
import org.nico.noson.entity.NoType;
import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.client.entity.User;
import org.nico.ratel.landlords.entity.Poker;
import org.nico.ratel.landlords.entity.PokerSell;
import org.nico.ratel.landlords.enums.PokerLevel;
import org.nico.ratel.landlords.enums.ServerEventCode;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.helper.PokerHelper;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.print.SimpleWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientEventListener_CODE_GAME_POKER_PLAY extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		Map<String, Object> map = MapHelper.parser(data);

		SimplePrinter.printNotice("这是你的时刻！你手里的筹码就在下面！ ");
		List<Poker> pokers = Noson.convert(map.get("pokers"), new NoType<List<Poker>>() {
		});
		SimplePrinter.printPokers(pokers);
		SimplePrinter.printNotice("记牌器：");
		SimplePrinter.printNotice(map.containsKey("lastPokers")?map.get("lastPokers").toString():"");

		SimplePrinter.printNotice("来吧！出招！战斗到死为止！ (输入 E 逃离地狱 输入 P 进行时间越过  输入 V 开启鹰眼)");
		String line = SimpleWriter.write(User.INSTANCE.getNickname(), "combination");

		if (line == null) {
			SimplePrinter.printNotice("无效输入");
			call(channel, data);
		} else {
			if (line.equalsIgnoreCase("pass") || line.equalsIgnoreCase("p")) {
				pushToServer(channel, ServerEventCode.CODE_GAME_POKER_PLAY_PASS);
			} else if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("e")) {
				pushToServer(channel, ServerEventCode.CODE_CLIENT_EXIT);
			} else if (line.equalsIgnoreCase("view") || line.equalsIgnoreCase("v")) {
				if (!map.containsKey("lastSellPokers") || !map.containsKey("lastSellClientId")) {
					SimplePrinter.printNotice("当前服务端版本不支持此功能，需要 v1.2.4 以上版本。");
					call(channel, data);
					return;
				}
				Object lastSellPokersObj = map.get("lastSellPokers");
				if (lastSellPokersObj == null || Integer.valueOf(SimpleClient.id).equals(map.get("lastSellClientId"))) {
					SimplePrinter.printNotice("轮到你先出牌！");
					call(channel, data);
					return;
				} else {
					List<Poker> lastSellPokers = Noson.convert(lastSellPokersObj, new NoType<List<Poker>>() {});
					List<PokerSell> sells = PokerHelper.validSells(PokerHelper.checkPokerType(lastSellPokers), pokers);
					if (sells.size() == 0) {
						SimplePrinter.printNotice("很遗憾，没有能打过的牌型组合...");
						call(channel, data);
						return;
					}
					for (int i = 0; i < sells.size(); i++) {
						SimplePrinter.printNotice(i + 1 + ". " + PokerHelper.textOnlyNoType(sells.get(i).getSellPokers()));
					}
					while (true) {
						SimplePrinter.printNotice("输入序号选择出牌组合 (输入 [back|b] 返回)");
						line = SimpleWriter.write(User.INSTANCE.getNickname(), "choose");
						if (line.equalsIgnoreCase("back") || line.equalsIgnoreCase("b")) {
							call(channel, data);
							return;
						} else {
							try {
								int choose = Integer.valueOf(line);
								if (choose < 1 || choose > sells.size()) {
									SimplePrinter.printNotice("输入的数字必须在 1 到 " + sells.size() + " 之间。");
								} else {
									List<Poker> choosePokers = sells.get(choose - 1).getSellPokers();
									List<Character> options = new ArrayList<>();
									for (Poker poker : choosePokers) {
										options.add(poker.getLevel().getAlias()[0]);
									}
									pushToServer(channel, ServerEventCode.CODE_GAME_POKER_PLAY, Noson.reversal(options.toArray(new Character[]{})));
									break;
								}
							} catch (NumberFormatException e) {
								SimplePrinter.printNotice("请输入数字。");
							}
						}
					}
				}

//				PokerHelper.validSells(lastPokerSell, pokers);
			} else {
				String[] strs = line.split(" ");
				List<Character> options = new ArrayList<>();
				boolean access = true;
				for (int index = 0; index < strs.length; index++) {
					String str = strs[index];
					if (str.equalsIgnoreCase("10")) {
						// 统一将 10 转换为别名 '0'
						options.add('0');
						continue;
					}

					for (char c : str.toCharArray()) {
						if (c == ' ' || c == '\t') {
							continue;
						}
						if (!PokerLevel.aliasContains(c)) {
							access = false;
							break;
						} else {
							options.add(c);
						}
					}
				}
			if (access) {
				pushToServer(channel, ServerEventCode.CODE_GAME_POKER_PLAY, Noson.reversal(options.toArray(new Character[]{})));
			} else {
				SimplePrinter.printNotice("无效输入");

					if (lastPokers != null) {
						SimplePrinter.printNotice("是致胜一手还是自我毁灭之道？");
						String typeZh = lastSellClientType;
						try{ typeZh = org.nico.ratel.landlords.enums.ClientType.valueOf(typeZh).zh(); }catch(Exception ignore){}
						SimplePrinter.printNotice(lastSellClientNickname + "[" + typeZh + "] 使用了:");
						SimplePrinter.printPokers(lastPokers);
					}

					call(channel, data);
				}
			}
		}

	}

}
