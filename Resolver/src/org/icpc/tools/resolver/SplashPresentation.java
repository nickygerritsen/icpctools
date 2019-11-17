package org.icpc.tools.resolver;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.icpc.tools.contest.model.IContest;
import org.icpc.tools.contest.model.IProblem;
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
	protected Font mediumFont;

	private BufferedImage image;

	@Override
	public void init() {
		float dpi = 96;
		float inch = height * 72f / dpi / 10f;
		titleFont = ICPCFont.getMasterFont().deriveFont(Font.BOLD, inch * 1f);
		attrFont = ICPCFont.getMasterFont().deriveFont(Font.PLAIN, inch * 0.4f);
		mediumFont = ICPCFont.getMasterFont().deriveFont(Font.BOLD, inch * 0.55f);
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

		g.setColor(new Color(96, 131, 194));
		g.setFont(titleFont);
		IContest contest = getContest();
		String s;
		FontMetrics fm = g.getFontMetrics();

		if (contest != null) {
			TreeMap<String, Integer> perProblemCount = new TreeMap<>();
			ISubmission[] submissions = contest.getSubmissions();
			int count = 0;
			for (ISubmission submission : submissions) {
				if (!contest.isJudged(submission)) {
					count++;
					String problemId = submission.getProblemId();
					if (perProblemCount.get(problemId) == null) {
						perProblemCount.put(problemId, 0);
					}
					perProblemCount.put(problemId, perProblemCount.get(problemId) + 1);
				}
			}
			s = Messages.getString("splashPending").replace("{0}", count + "");
			g.drawString(s, (width - fm.stringWidth(s)) / 2, h + BORDER + fm.getHeight());

			StringBuilder problemCountStringBuilder = new StringBuilder();
			problemCountStringBuilder.append("   ");
			for(Map.Entry<String,Integer> entry : perProblemCount.entrySet()) {
				IProblem problem = contest.getProblemById(entry.getKey());
				problemCountStringBuilder.append(problem.getLabel());
				problemCountStringBuilder.append(": ");
				problemCountStringBuilder.append(entry.getValue());
				problemCountStringBuilder.append("    ");
			}
			g.setFont(mediumFont);
			FontMetrics fm2 = g.getFontMetrics();
			String problemCountString = problemCountStringBuilder.toString();
			g.drawString(problemCountString, (width - fm2.stringWidth(problemCountString)) / 2, h + BORDER + 2*fm.getHeight());
		}

		// ----- Attribution -----
		g.setColor(Color.WHITE);
		g.setFont(smallFont);
		g.drawString(conceptBy + ": " + conceptAttr1 + "/" + conceptAttr2 + " (" + conceptOrg + ")", BORDER, height-BORDER);
		String implString = implBy + ": " + implAttr + " (" + implOrg + ")";
		FontMetrics fm2 = g.getFontMetrics();
		g.drawString(implString, width-BORDER-fm2.stringWidth(implString), height-BORDER);
	}
}
