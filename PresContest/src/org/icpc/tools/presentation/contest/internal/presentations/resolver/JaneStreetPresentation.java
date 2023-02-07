package org.icpc.tools.presentation.contest.internal.presentations.resolver;

import org.icpc.tools.contest.Trace;
import org.icpc.tools.contest.model.IContest;
import org.icpc.tools.presentation.contest.internal.AbstractICPCPresentation;
import org.icpc.tools.presentation.contest.internal.ImageHelper;
import org.icpc.tools.presentation.contest.internal.ImageScaler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JaneStreetPresentation extends AbstractICPCPresentation {
	private BufferedImage logo;
	private static final Color BG_COLOR = new Color(0, 0, 0, 196);

	@Override
	public void setSize(Dimension d) {
		super.setSize(d);

		IContest contest = getContest();
		if (contest == null) {
			return;
		}

		String filename = "janestreet.png";

		try {
			BufferedImage fullLogo = ImageHelper.loadImage("/presentation/janestreet/" + filename);
			logo = ImageScaler.scaleImage(fullLogo, width, height);
		} catch (Exception e) {
			Trace.trace(Trace.ERROR, "Could not load image " + filename + ": " + e.getMessage());
			logo = getContest().getLogoImage(width, height, true, true);
			if (logo == null) {
				ClassLoader cl = getClass().getClassLoader();
				try {
					logo = ImageScaler.scaleImage(ImageIO.read(cl.getResource("images/id.png")), width, height);
				} catch (Exception ee) {
					Trace.trace(Trace.ERROR, "Error loading images", ee);
				}
			}
		}
	}

	@Override
	public void paint(Graphics2D g) {
		g.drawImage(logo, (width - logo.getWidth()) / 2, 0, null);
	}
}
