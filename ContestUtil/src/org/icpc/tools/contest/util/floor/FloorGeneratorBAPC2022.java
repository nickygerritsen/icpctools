package org.icpc.tools.contest.util.floor;

import org.icpc.tools.contest.Trace;
import org.icpc.tools.contest.model.FloorMap;
import org.icpc.tools.contest.model.IContest;
import org.icpc.tools.contest.model.IPrinter;
import org.icpc.tools.contest.model.ITeam;
import org.icpc.tools.contest.model.feed.DiskContestSource;

import java.io.File;

public class FloorGeneratorBAPC2022 extends FloorGenerator {
	// table width (in meters). ICPC standard is 1.8
	private static final float tw = 1.8f;

	// table depth (in meters). ICPC standard is 0.8
	private static final float td = 0.8f;

	// team area width (in meters). ICPC standard is 3.0
	private static final float taw = 3.0f;

	// team area depth (in meters). ICPC standard is 2.2
	private static final float tad = 2.2f;

	private static final int numRooms = 4;
	private static final int firstRoom = 13;
	private static final int numRows = 7;
	private static final int numCols = 2;
	private static final int numProblems = 12;
	private static final float innerRoomSpace = 3f;
	private static final float magicXDiff = .6f;
	private static final float magicXDiff2 = 1.6f;

	private static final boolean showTeams = true;

	// If > 0, use balloons with these numbers
	private static final int useIntegerBalloons = -1;

	private static final FloorMap floor = new FloorMap(taw - .2f, tad - .2f, tw, td);

	private static final String balloon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	protected static void createTeamRow(int num, int startingId, float x, float y, float dx, float dy, short rotation) {
		for (int i = 0; i < num; i++) {
			floor.createTeam(startingId + i, x + dx * i, y + dy * i, rotation);
		}
	}

	protected static void createTeamRowRev(int num, int startingId, float x, float y, float dx, float dy,
										   short rotation) {
		for (int i = 0; i < num; i++) {
			floor.createTeam(startingId - i, x + dx * i, y + dy * i, rotation);
		}
	}

	protected static void createAdjacentTeam(int teamNumber, int newId, double dx, double dy) {
		ITeam t = floor.getTeam(teamNumber);
		floor.createTeam(newId, t.getX() + dx, t.getY() + dy, t.getRotation());
	}

	public static void main(String[] args) {
		Trace.init("ICPC Floor Map Generator for BAPC 2022", "floorMap", args);

		try {

			float roomHeight = numCols * taw;
			float roomWidth = numRows * tad;
			float bottomRoomsStart = roomHeight + (20f * (numProblems + 1) / 12f);
			float totalRoomsWidth = numRooms * roomWidth + numRooms * innerRoomSpace - 0.8f * innerRoomSpace;

			for (int i = 0; i < numProblems; i++) {
				float x = totalRoomsWidth + 1;
				float y = roomHeight + 20f * (i + 1) / 12f;
				if (useIntegerBalloons > 0) {
					floor.createBalloon((i + useIntegerBalloons) + "", x, y);
				} else {
					floor.createBalloon(balloon.charAt(i) + "", x, y);
				}
			}

			IPrinter p = floor.createPrinter(roomWidth + innerRoomSpace / 2f, roomHeight + 1);

			floor.createAisle(totalRoomsWidth, roomHeight, totalRoomsWidth, bottomRoomsStart);
			floor.createAisle(0, roomHeight, totalRoomsWidth, roomHeight);

			int room = firstRoom;

			for (int i = 0; i < numRooms; i++) {
				float roomTop = 0;
				float roomLeft = i * (roomWidth + innerRoomSpace);
				for (int j = 0; j < numRows; j++) {
					float x = roomLeft + (j + 1) * tad;
					float y1 = roomTop + taw / 2f;
					if (i == 2) {
						y1 -= taw;
					}
					float y2;
					if (j == 0 || j == numRows - 1) {
						y2 = roomHeight;
					} else {
						y2 = roomHeight - taw / 2f;
					}
					floor.createAisle(x, y1, x, y2);
				}

				for (int j = i == 2 ? 0 : 1; j < numCols; j++) {
					float y = roomTop + j * taw;
					floor.createAisle(roomLeft + tad, y, roomLeft + numRows * tad, y);
				}

				if (showTeams) {
					for (int r = (i == 2 ? 1 : 0); r < numRows; r++) {
						for (int c = 0; c < (i == 2 ? numCols + 1 : numCols); c++) {
							int roomStart = switch (room) {
								case 13 -> 47;
								case 14 -> 33;
								case 15 -> 15;
								case 16 -> 1;
								default -> -1;
							};
							int numInRoom = (i == 2 ? numCols + 1 : numCols) * numRows;
							int lastInRoom = roomStart + numInRoom - 1;
							int innerRoomIndex = r * (i == 2 ? numCols + 1 : numCols) + ((i == 2 ? numCols + 1 : numCols) - 1 - c);
							int teamId = lastInRoom - innerRoomIndex;
							float x = roomLeft + tad - magicXDiff + (r * tad);
							float y = roomTop + taw / 2f + ((numCols - c - 1) * taw);
							floor.createTeam(teamId, x, y, FloorMap.E);
						}
					}
				}

				room++;
			}


			if (args != null && args.length > 0) {
				File f = new File(args[0]);
				DiskContestSource source = new DiskContestSource(f);
				IContest contest2 = source.getContest();
				source.waitForContest(10000);

				floor.write(f);
			}

			Trace.trace(Trace.USER, "------------------");

			long time = System.currentTimeMillis();
			FloorMap.Path path1 = floor.getPath(floor.getTeam(118), floor.getTeam(6));
			FloorMap.Path path2 = floor.getPath(floor.getTeam(37), p);

			Trace.trace(Trace.USER, "Time: " + (System.currentTimeMillis() - time));

			show(floor, 37, true, path1, path2);
//            show(floor, 37, true, path1);
		} catch (Exception e) {
			Trace.trace(Trace.ERROR, "Error generating floor map", e);
		}
	}
}
