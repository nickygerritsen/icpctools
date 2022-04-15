package org.icpc.tools.contest.util.floor;

import org.icpc.tools.contest.Trace;
import org.icpc.tools.contest.model.FloorMap;
import org.icpc.tools.contest.model.IPrinter;
import org.icpc.tools.contest.model.feed.JSONParser;
import org.icpc.tools.contest.model.feed.JSONParser.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FloorGeneratorSWERC extends FloorGenerator {
	// team area width (in meters). ICPC standard is 3.0
	private static final float taw = 3.3f;

	// team area depth (in meters). ICPC standard is 2.2
	private static final float tad = 2.5f;

	private static final float aisle = 2f + tad * 3 / 2;

	private static final FloorMap floor = new FloorMap(taw, tad, 3.3, 0.95);

	private static final int teamsPerRowSmallRooms = 3;

	private static final int teamsPerRowLargeRoom = 2;

	private static final int smallRoomRows = 6;

	private static final int largeRoomRows = 16;

	private static final float interRoomSpacingX = (aisle + tad) / 2 * 3;
	private static final float interRoomSpacingY = taw * 2;

	private static class RoomPos {
		private final int row;
		private final int pc;
		private final int room;
		private final int hashCode;

		public RoomPos(int row, int pc, int room) {
			this.row = row;
			this.pc = pc;
			this.room = room;
			this.hashCode = Objects.hash(row, pc, room);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			RoomPos that = (RoomPos) o;
			return row == that.row && pc == that.pc && room == that.room;
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

	private static final HashMap<RoomPos, String> present = new HashMap<>();

	protected static float createAisleOfTeams(int room, int row, float xx, float y) {
		float x = xx;
		x += aisle / 2;
		for (int pc = 0; pc < teamsPerRowSmallRooms; pc++) {
			RoomPos rp = new RoomPos(row, pc + 1, room);
			int teamId = Integer.parseInt(present.getOrDefault(rp, "0"));
			if (teamId > 0) {
				floor.createTeamRow(1, teamId, x, y - taw * pc, FloorMap.W, true, true);
			}
		}
		x += tad / 2;

		floor.createAisle(x + aisle / 4, y - (teamsPerRowSmallRooms) * taw, x + aisle / 4, y + taw);

		return x;
	}

	protected static float createAisleOfTeamsSkippingMiddle(int room, int row, float xx, float y) {
		y -= taw;
		float x = xx;
		x += aisle / 2;
		for (int pc = 0; pc < teamsPerRowLargeRoom; pc++) {
			RoomPos rp = new RoomPos(row, pc + 1, room);
			int teamId = Integer.parseInt(present.getOrDefault(rp, "0"));
			if (teamId > 0) {
				floor.createTeamRow(1, teamId, x, y + taw * (FloorGeneratorSWERC.teamsPerRowLargeRoom - pc - 1) * 2, FloorMap.W, true, true);
			}
		}
		x += tad / 2;

		floor.createAisle(x + aisle / 4, y - (teamsPerRowSmallRooms - 2) * taw, x + aisle / 4, y + 3 * taw);

		return x;
	}

	protected static void createSmallRoom(int room, float x, float y) {
		floor.createAisle(x + aisle / 4, y - (teamsPerRowSmallRooms) * taw, x + aisle / 4, y + taw);
		float roomStartX = x;
		for (int i = 0; i < smallRoomRows; i++) {
			x = createAisleOfTeams(room, i + 1, x, y);
		}
		floor.createAisle(roomStartX + aisle / 4, y - (teamsPerRowSmallRooms) * taw, x + aisle / 4, y - (teamsPerRowSmallRooms) * taw);
		floor.createAisle(roomStartX + aisle / 4, y + taw, x + aisle / 4, y + taw);
	}

	protected static void createLargeRoom(int room, float x, float y) {
		floor.createAisle(x + aisle / 4, y - (teamsPerRowLargeRoom) * taw, x + aisle / 4, y + 2 * taw);
		float roomStartX = x;
		for (int i = 0; i < largeRoomRows; i++) {
			x = createAisleOfTeamsSkippingMiddle(room, i + 1, x, y);
		}
		floor.createAisle(roomStartX + aisle / 4, y - (teamsPerRowLargeRoom) * taw, x + aisle / 4, y - (teamsPerRowLargeRoom) * taw);
		floor.createAisle(roomStartX + aisle / 4, y - (teamsPerRowLargeRoom - 2) * taw, x + aisle / 4, y - (teamsPerRowLargeRoom - 2) * taw);
		floor.createAisle(roomStartX + aisle / 4, y - (teamsPerRowLargeRoom - 4) * taw, x + aisle / 4, y - (teamsPerRowLargeRoom - 4) * taw);
	}

	public static void main(String[] args) {
		Trace.init("ICPC Floor Map Generator", "floorMap", args);

		try {
			File positions = new File("/Users/nicky/Projects/SWERC/swerc-ccs-config-2021-2022/rooms/positions.json");
			JSONParser p = new JSONParser(positions);
			Object[] teams = p.readArray();
			for (int i = 0; i < teams.length; i++) {
				JsonObject team = (JsonObject) teams[i];
				String room = team.getString("room").substring(5);
				String row = team.getString("row");
				String pc = team.getString("pc");
				String teamId = "" + (i + 1);
				RoomPos rp = new RoomPos(Integer.parseInt(row), Integer.parseInt(pc), Integer.parseInt(room));
				present.put(rp, teamId);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			float x = 0;
			float y = 0;

			int numProblems = 11;
			List<String> problems = new ArrayList<>();
			for (char letter = 'A'; letter < 'A' + numProblems; letter++) {
				problems.add(String.valueOf(letter));
			}

			float largeRoomHeight = (teamsPerRowLargeRoom + 2) * taw;
			float largeRoomWidth = largeRoomRows * (aisle + tad) / 2;
			float firstLargeRoomOffset = 0;
			float secondLargeRoomOffset = firstLargeRoomOffset + largeRoomHeight + interRoomSpacingY;
			float smallRoomsOffset = secondLargeRoomOffset + taw + largeRoomHeight + interRoomSpacingY;
			float printerAndBalloonX = largeRoomWidth + aisle;

			x = 0;
			y = smallRoomsOffset;
			createSmallRoom(3, x, y);

			float secondSmallRoomOffset = (aisle + tad) / 2 * smallRoomRows + interRoomSpacingX;
			x = secondSmallRoomOffset;
			y = smallRoomsOffset;
			createSmallRoom(4, x, y);

			x = 0;
			y = secondLargeRoomOffset;
			createLargeRoom(5, x, y);

			x = 0;
			y = 0;
			createLargeRoom(6, x, y);

			Trace.trace(Trace.USER, "Num teams: " + present.size());

			// Create inter-room aisles

			// Small room aisles
			floor.createAisle(aisle / 2, smallRoomsOffset - 4 * taw, aisle / 2, smallRoomsOffset - 3 * taw);
			floor.createAisle(aisle / 2 + secondSmallRoomOffset, smallRoomsOffset - 4 * taw, aisle / 2 + secondSmallRoomOffset, smallRoomsOffset - 3 * taw);
			floor.createAisle(aisle / 2, smallRoomsOffset - 4 * taw, printerAndBalloonX, smallRoomsOffset - 4 * taw);

			// First large room aisles
			floor.createAisle(aisle / 2, smallRoomsOffset - 4 * taw, aisle / 2, secondLargeRoomOffset + largeRoomHeight - taw * 2);
			floor.createAisle(largeRoomWidth - (aisle + tad) / 2, smallRoomsOffset - 4 * taw, largeRoomWidth - (aisle + tad) / 2, secondLargeRoomOffset + largeRoomHeight - taw * 2);

			// Second large room aisles
			floor.createAisle(aisle / 2, largeRoomHeight - 2 * taw, aisle / 2, largeRoomHeight - taw);
			floor.createAisle(largeRoomWidth - (aisle + tad) / 2, largeRoomHeight - 2 * taw, largeRoomWidth - (aisle + tad) / 2, largeRoomHeight - taw);
			floor.createAisle(aisle / 2, largeRoomHeight - taw, printerAndBalloonX, largeRoomHeight - taw);

			// Right aisle
			floor.createAisle(printerAndBalloonX, largeRoomHeight - taw, printerAndBalloonX, smallRoomsOffset - 4 * taw);

			float firstLargeRoomCenter = secondLargeRoomOffset + largeRoomHeight / 2 - 2 * taw;
			float totalProblemHeight = (problems.size() - 1) * 2;
			float firstProblemPosition = firstLargeRoomCenter - totalProblemHeight / 2;

			for (int i = 0; i < problems.size(); i++) {
				floor.createBalloon(problems.get(i), printerAndBalloonX + tad, firstProblemPosition + i * 2);
			}

			IPrinter p = floor.createPrinter(printerAndBalloonX + tad, firstProblemPosition + problems.size() * 2);

			long time = System.currentTimeMillis();

			Trace.trace(Trace.USER, "Time: " + (System.currentTimeMillis() - time));

			if (args != null && args.length > 0) {
				File f = new File(args[0]);
				floor.write(f);
			}

			FloorMap.Path path = floor.getPath(floor.getTeam(57), p);

			show(floor, 57, true, path);
		} catch (Exception e) {
			Trace.trace(Trace.ERROR, "Error generating floor map", e);
		}
	}
}
