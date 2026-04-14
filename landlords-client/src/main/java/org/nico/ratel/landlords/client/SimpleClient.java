package org.nico.ratel.landlords.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import org.nico.ratel.landlords.client.proxy.ProtobufProxy;
import org.nico.ratel.landlords.client.proxy.WebsocketProxy;
import org.nico.ratel.landlords.features.Features;
import org.nico.ratel.landlords.print.SimplePrinter;

public class SimpleClient {

	public static int id = -1;

	public final static String VERSION = Features.VERSION_1_3_0;

	public static String serverAddress = "127.0.0.1";

	public static int port = 1024;

	public static String protocol = "pb";


	public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
		if (args != null && args.length > 0) {
			for (int index = 0; index < args.length; index = index + 2) {
				if (index + 1 < args.length) {
					if (args[index].equalsIgnoreCase("-p") || args[index].equalsIgnoreCase("-port")) {
						port = Integer.parseInt(args[index + 1]);
					}
					if (args[index].equalsIgnoreCase("-h") || args[index].equalsIgnoreCase("-host")) {
						serverAddress = args[index + 1];
					}
					if (args[index].equalsIgnoreCase("-ptl") || args[index].equalsIgnoreCase("-protocol")) {
						protocol = args[index + 1];
					}
				}
			}
		}
		SimplePrinter.printNotice("正在连接本地服务器 " + serverAddress + ":" + port + " ...");

		if (Objects.equals(protocol, "pb")) {
			new ProtobufProxy().connect(serverAddress, port);
		} else if (Objects.equals(protocol, "ws")) {
			new WebsocketProxy().connect(serverAddress, port + 1);
		} else {
			throw new UnsupportedOperationException("Unsupported protocol " + protocol);
		}
	}


}
