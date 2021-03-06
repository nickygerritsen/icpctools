package org.icpc.tools.contest.util.problemset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Uses SnakeYAML to parse the contest YAML files.
 */
public class YamlUtil {
	private static final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	public static List<Problem> readProblemSet(File f) throws IOException {
		if (f == null || !f.exists())
			throw new FileNotFoundException("Problem set not found");

		BufferedReader br = null;
		List<Problem> problems = new ArrayList<>();
		try {
			br = new BufferedReader(new FileReader(f));

			Yaml yaml = new Yaml();
			Object obj = yaml.load(br);

			// the file should have a top-level map of problems, which contains a list of problems
			if (!(obj instanceof Map<?, ?>))
				throw new IOException("Problem set not imported: invalid format");

			Map<?, ?> map = (Map<?, ?>) obj;
			obj = map.get("problems");
			if (obj == null || !(obj instanceof List<?>))
				throw new IOException("Problem set not imported: no problems");

			List<?> list = (List<?>) obj;
			for (Object o : list) {
				if (o instanceof Map<?, ?>) {
					map = (Map<?, ?>) o;

					try {
						Problem problem = new Problem();
						problem.letter = (String) map.get("letter");
						problem.shortName = (String) map.get("short-name");
						problem.color = (String) map.get("color");
						Object ob = map.get("rgb");
						if (ob instanceof String) {
							String rgb = (String) ob;
							if (rgb.startsWith("#"))
								rgb = rgb.substring(1);
							problem.setRGB(rgb);
						} else if (ob instanceof Integer) {
							problem.setRGB(((Integer) ob).toString());
						}
						problems.add(problem);
					} catch (Exception e) {
						throw new IOException("Problem set not be parsed: " + e.getMessage());
					}
				}
			}
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception e) {
				// ignore
			}
		}
		return problems;
	}

	private static String notNull(String s) {
		if (s == null)
			return "";

		return s;
	}

	public static void writeProblemSet(File f, List<Problem> problems) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));

		bw.write("# Generated by ICPC Problem Set editor (" + format.format(new Date()) + ")\n\n");
		bw.write("problems:");

		for (Problem p : problems) {
			bw.write("\n");
			bw.write("  - letter:     " + notNull(p.letter) + "\n");
			bw.write("    short-name: " + notNull(p.shortName) + "\n");
			bw.write("    color:      " + notNull(p.color) + "\n");
			String s = notNull(p.getRGB());
			if (s.length() > 0)
				s = "'#" + s + "'";
			bw.write("    rgb:        " + s + "\n");
		}

		bw.close();
	}
}