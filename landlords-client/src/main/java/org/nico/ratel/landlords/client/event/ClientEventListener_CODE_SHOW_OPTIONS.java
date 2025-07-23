package org.nico.ratel.landlords.client.event;

import org.nico.ratel.landlords.client.entity.User;
import org.nico.ratel.landlords.enums.ClientEventCode;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.print.SimpleWriter;
import org.nico.ratel.landlords.utils.OptionsUtils;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_SHOW_OPTIONS extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		SimplePrinter.printNotice("Options: ");
		SimplePrinter.printNotice("1. 线上对决(暂未开放)");
		SimplePrinter.printNotice("2. 自我超越");
		SimplePrinter.printNotice("3. 设置");
		SimplePrinter.printNotice("请选择要进行对决的模式！ (输入 [exit|e] 注销)");
		String line = SimpleWriter.write(User.INSTANCE.getNickname(), "selection");

		if(line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("e")) {
			System.exit(0);
		} else {
			int choose = OptionsUtils.getOptions(line);
			if (choose == 1) {
				get(ClientEventCode.CODE_SHOW_OPTIONS_PVP).call(channel, data);
			} else if (choose == 2) {
				get(ClientEventCode.CODE_SHOW_OPTIONS_PVE).call(channel, data);
			} else if (choose == 3) {
				get(ClientEventCode.CODE_SHOW_OPTIONS_SETTING).call(channel, data);
			} else {
				SimplePrinter.printNotice("Invalid option, please choose again：");
				call(channel, data);
			}
		}
	}
}
