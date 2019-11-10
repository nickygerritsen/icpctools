package org.icpc.tools.resolver;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.icpc.tools.contest.model.IContest;
import org.icpc.tools.contest.model.ISubmission;
import org.icpc.tools.presentation.contest.internal.AbstractICPCPresentation;
import org.icpc.tools.presentation.contest.internal.ICPCColors;
import org.icpc.tools.presentation.contest.internal.ICPCFont;
import org.icpc.tools.presentation.contest.internal.ImageScaler;
import org.icpc.tools.presentation.contest.internal.ShadedRectangle;

public class SplashPresentation extends AbstractICPCPresentation {
	private static final String title = Messages.getString("splashTitle");
	private static final String conceptBy = Messages.getString("splashConceptBy");
	private static final String conceptAttr1 = Messages.getString("splashConceptAttr1");
	private static final String conceptAttr2 = Messages.getString("splashConceptAttr2");
	private static final String conceptOrg = Messages.getString("splashConceptOrg");
	private static final String implBy = Messages.getString("splashImplBy");
	private static final String implAttr = Messages.getString("splashImplAttr");
	private static final String implOrg = Messages.getString("splashImplOrg");

	protected Font titleFont;
	protected Font attrFont;
	protected Font smallFont;

	private BufferedImage image;

	@Override
	public void init() {
		float dpi = 96;
		float inch = height * 72f / dpi / 10f;
		titleFont = ICPCFont.getMasterFont().deriveFont(Font.BOLD, inch * 1f);
		attrFont = ICPCFont.getMasterFont().deriveFont(Font.PLAIN, inch * 0.4f);
		smallFont = ICPCFont.getMasterFont().deriveFont(Font.BOLD, inch * 0.3f);

		image = getContest().getLogoImage((int) (width * 0.75), (int) (height * 0.5), true, true);

		if (image == null)
			image = getIdImage();
	}

	private BufferedImage getIdImage() {
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/id.png");
			return ImageScaler.scaleImage(ImageIO.read(in), width * 0.3, height * 0.3);
		} catch (Exception e) {
			// ignore
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

		return null;
	}

	@Override
	public long getDelayTimeMs() {
		return 30000;
	}

	@Override
	public void paint(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		int BORDER = height / 20;
		int topOffset = 0;
		int h = BORDER + topOffset;

		if (image != null) {
			g.drawImage(image, (width - image.getWidth()) / 2, h, null);
			h += image.getHeight();
		}

		g.setColor(Color.WHITE);
		g.setFont(titleFont);
		IContest contest = getContest();
		String s;
		FontMetrics fm = g.getFontMetrics();

		if (contest != null) {
			ISubmission[] submissions = contest.getSubmissions();
			int count = 0;
			for (ISubmission submission : submissions) {
				if (!contest.isJudged(submission))
					count++;
			}
			s = Messages.getString("splashPending").replace("{0}", count + "");
			g.drawString(s, (width - fm.stringWidth(s)) / 2, h + BORDER + fm.getHeight());
		}

		// ----- Attribution -----
		g.setColor(Color.WHITE);
		g.setFont(attrFont);
		fm = g.getFontMetrics();

		int GAP = fm.getHeight() / 3;
		h = (int) (height - BORDER - fm.getHeight() * 5.5f - GAP * 2);

		ShadedRectangle.drawRoundRect(g, (width - fm.stringWidth(title)) / 2 - GAP, h - fm.getAscent() - GAP,
				fm.stringWidth(title) + GAP * 2, fm.getAscent() + GAP * 2, ICPCColors.PENDING[ICPCColors.CCOUNT / 3]);
		g.drawString(title, (width - fm.stringWidth(title)) / 2, h);
		h += (fm.getHeight() * 2f + GAP);

		int ww = Math.max(fm.stringWidth(conceptAttr1), fm.stringWidth(conceptAttr2)) + fm.stringWidth(implAttr)
				+ BORDER * 3;
		int col1 = (width - ww) / 2;
		int col2 = (width + ww) / 2;

		g.setFont(smallFont);
		FontMetrics fm2 = g.getFontMetrics();

		GAP = fm2.getHeight() / 3;
		ShadedRectangle.drawRoundRect(g, col1 - GAP, h - fm2.getAscent() - GAP, fm2.stringWidth(conceptBy) + GAP * 2,
				fm2.getHeight() + (int) (GAP * 1.7f), ICPCColors.SOLVED[ICPCColors.CCOUNT / 3]);
		g.setColor(Color.WHITE);
		g.drawString(conceptBy, col1, h);

		ShadedRectangle.drawRoundRect(g, col2 - fm2.stringWidth(implBy) - GAP, h - fm2.getAscent() - GAP,
				fm2.stringWidth(implBy) + GAP * 2, fm2.getHeight() + (int) (GAP * 1.7f),
				ICPCColors.SOLVED[ICPCColors.CCOUNT / 3]);
		g.setColor(Color.WHITE);
		g.setFont(smallFont);
		g.drawString(implBy, col2 - fm2.stringWidth(implBy), h);
		h += (fm.getHeight() * 1.5f + GAP);

		g.setColor(Color.WHITE);
		g.setFont(attrFont);
		g.drawString(conceptAttr1, col1, h);

		g.setColor(Color.WHITE);
		g.setFont(attrFont);
		g.drawString(implAttr, col2 - fm.stringWidth(implAttr), h);
		h += fm.getHeight();

		g.setColor(Color.WHITE);
		g.setFont(attrFont);
		g.drawString(conceptAttr2, col1, h);

		g.setFont(smallFont);
		g.drawString(implOrg, col2 - fm2.stringWidth(implOrg), h);
		h += fm.getHeight();

		g.setFont(smallFont);
		g.drawString(conceptOrg, col1, h);
	}
}