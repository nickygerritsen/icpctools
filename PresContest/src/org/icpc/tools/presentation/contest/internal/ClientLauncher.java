package org.icpc.tools.presentation.contest.internal;

import java.awt.image.BufferedImage;
import java.util.List;

import org.icpc.tools.client.core.IConnectionListener;
import org.icpc.tools.contest.Trace;
import org.icpc.tools.contest.model.feed.ContestSource;
import org.icpc.tools.contest.model.feed.RESTContestSource;
import org.icpc.tools.contest.model.util.ArgumentParser;
import org.icpc.tools.contest.model.util.ArgumentParser.OptionParser;
import org.icpc.tools.presentation.contest.internal.standalone.TeamUtil;
import org.icpc.tools.presentation.core.DisplayConfig;
import org.icpc.tools.presentation.core.PresentationWindow;
import org.icpc.tools.presentation.core.internal.PresentationWindowImpl;

public class ClientLauncher {
	protected static PresentationClient instance;

	public static PresentationClient getInstance() {
		return instance;
	}

	protected static void showHelp() {
		System.out.println("Usage: client.bat/sh cdsURL user password [options]");
		System.out.println();
		System.out.println("  Options:");
		System.out.println("     --name name");
		System.out.println("         Give this client a name, e.g. \"Stage right\" or \"Site 2\"");
		System.out.println("     --display #");
		System.out.println("         Use the specified display");
		System.out.println("         1 = primary display, 2 = secondary display, etc.");
		System.out.println("     --multi-display p@wxh");
		System.out.println("         This presentation is stretched across multiple client displays");
		System.out.println("         Use \"2@3x2\" to indicate this client is position 2 (top middle) in a 3x2 grid");
		System.out.println("     --fps");
		System.out.println("         Show the frame rate on screen");
		System.out.println("     --help");
		System.out.println("         Shows this message");
		System.out.println("     --version");
		System.out.println("         Displays version information");
	}

	public static void main(final String[] args) {
		Trace.init("ICPC Presentation Client", "client", args);
		System.setProperty("apple.awt.application.name", "Presentation Client");

		String[] nameStr = new String[1];
		final String[] displayStr = new String[2];
		boolean[] showFPS = new boolean[1];
		ContestSource contestSource = ArgumentParser.parse(args, new OptionParser() {
			@Override
			public boolean setOption(String option, List<Object> options) throws IllegalArgumentException {
				if ("--name".equals(option)) {
					ArgumentParser.expectOptions(option, options, "name:string");
					nameStr[0] = (String) options.get(0);
					return true;
				} else if ("--display".equals(option)) {
					ArgumentParser.expectOptions(option, options, "#:string");
					displayStr[0] = (String) options.get(0);
					return true;
				} else if ("--multi-display".equals(option)) {
					ArgumentParser.expectOptions(option, options, "p@wxh:string");
					displayStr[1] = (String) options.get(0);
					return true;
				} else if ("--fps".equals(option)) {
					showFPS[0] = true;
					return true;
				}
				return false;
			}

			@Override
			public void showHelp() {
				ClientLauncher.showHelp();
			}
		});

		RESTContestSource cdsSource = RESTContestSource.ensureCDS(contestSource);
		cdsSource.outputValidation();
		cdsSource.checkForUpdates("presentations-");

		String name = nameStr[0];
		PresentationClient client = null;
		int uid = -1;
		if ("team".equals(name)) {
			String teamLabel = TeamUtil.getTeamId();
			uid = Integer.parseInt(teamLabel);
			client = new PresentationClient(cdsSource, teamLabel, uid, "!admin");
		} else
			client = new PresentationClient(name, "!admin", cdsSource);
		instance = client;

		// open window
		createWindow(client, true, showFPS[0]);
		Trace.trace(Trace.INFO, client + " connecting to " + cdsSource);
		final PresentationClient client2 = client;

		client.addListener(new IConnectionListener() {
			@Override
			public void connectionStateChanged(boolean connected) {
				if (!connected)
					return;
				PresentationWindowImpl windowImpl = (PresentationWindowImpl) client2.window;
				if (client2.getUID() != -1)
					windowImpl.reduceThumbnails();
				if (connected && !windowImpl.isVisible()) {
					windowImpl.setDisplayConfig(new DisplayConfig(displayStr[0], displayStr[1]));
					windowImpl.openIt();
					client2.writeInfo();
				}
			}
		});
		client.connect();
	}

	protected static void createWindow(final PresentationClient client, final boolean sendthumbnails, boolean showFPS) {
		if (client.window != null)
			return;

		PresentationWindowImpl windowImpl = (PresentationWindowImpl) PresentationWindow.create();
		client.window = windowImpl;
		if (sendthumbnails)
			windowImpl.setThumbnailListener(new PresentationWindowImpl.IThumbnailListener() {
				@Override
				public void handleThumbnail(BufferedImage image) {
					client.writeInfoUpdate(image);
				}
			});
		windowImpl.showFPS(showFPS);
	}
}