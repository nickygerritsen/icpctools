package org.icpc.tools.contest.util.floor;

import org.icpc.tools.contest.Trace;
import org.icpc.tools.contest.model.FloorMap;
import org.icpc.tools.contest.model.ITeam;
import org.icpc.tools.contest.model.FloorMap.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FloorGeneratorNwerc2019 extends FloorGenerator {
    // table width (in meters). ICPC standard is 1.8
    private static final float tw = 1.8f;

    // table depth (in meters). ICPC standard is 0.8
    private static final float td = 0.8f;

    // team area width (in meters). ICPC standard is 3.0
    private static final float taw = 3.0f;

    // team area depth (in meters). ICPC standard is 2.2
    private static final float tad = 2.2f;

    private static final int numRooms = 8;
    private static final int firstRoom = 9;
    private static final int numRows = 7;
    private static final int numCols = 3;
    private static final int numProblems = 13;
    private static final float innerRoomSpace = 1f;
    private static final float magicXDiff = .6f;
    private static final float magicXDiff2 = 1.6f;

//    private static final boolean showTeams = false;
    private static final boolean showTeams = true;

    // If > 0, use balloons with these numbers
    private static final int useIntegerBalloons = -1;

    private static class SkippedTeam {
        int room;
        int row;
        int col;

        SkippedTeam(int room, int row, int col) {
            this.room = room;
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SkippedTeam))
                return false;

            SkippedTeam s = (SkippedTeam) o;
            return s.room == this.room && s.row == this.row && s.col == this.col;
        }
    }

    private static List<SkippedTeam> skippedTeams = new ArrayList<>();
    private static HashMap<Integer, Integer> teamsPerRoom = new HashMap<>();

    private static FloorMap floor = new FloorMap(taw - .2f, tad - .2f, tw, td);

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
        Trace.init("ICPC Floor Map Generator", "floorMap", args);

        try {
//            teamsPerRoom.put(9, 16);
//            teamsPerRoom.put(10, 16);
//            teamsPerRoom.put(11, 16);
//            teamsPerRoom.put(12, 16);
//            teamsPerRoom.put(13, 16);
//            teamsPerRoom.put(14, 16);
//            teamsPerRoom.put(15, 16);
//            teamsPerRoom.put(16, 16);
//
//            skippedTeams.add(new SkippedTeam(9, 3, 1));
//            skippedTeams.add(new SkippedTeam(9, 4, 1));
//            skippedTeams.add(new SkippedTeam(9, 4, 2));

            float roomHeight = numCols * taw;
            float roomWidth = numRows * tad;
            float bottomRoomsStart = roomHeight + (20f * (numProblems + 1) / 12f);
            float totalRoomsWidth = (numRooms / 2f) * roomWidth + ((numRooms / 2f)) * innerRoomSpace - 0.8f * innerRoomSpace;

            for (int i = 0; i < numProblems; i++) {
                float x = 0;
                float y = roomHeight + 20f * (i + 1) / 12f;
                if (useIntegerBalloons > 0) {
                    floor.createBalloon((i + useIntegerBalloons) + "", x, y);
                } else {
                    floor.createBalloon(balloon.charAt(i) + "", x, y);
                }
            }

            floor.createPrinter(2, (roomHeight + bottomRoomsStart) / 2f);

            floor.createAisle(1, roomHeight, 1, bottomRoomsStart);
            floor.createAisle(1, roomHeight, totalRoomsWidth + 1, roomHeight);
            floor.createAisle(1, bottomRoomsStart, totalRoomsWidth + 1, bottomRoomsStart);

            int teamId = 1;
            int room = firstRoom;

            // Top rooms
            for (int i = 0; i < numRooms / 2; i++) {
                float roomTop = 0;
                float roomLeft = 1 + i * (roomWidth + innerRoomSpace);
                for (int j = 0; j < numRows; j++) {
                    float x = roomLeft + (j + 1) * tad;
                    float y1 = roomTop + taw / 2f;
                    float y2;
                    if (j == 0 || j == numRows - 1) {
                        y2 = roomHeight;
                    } else {
                        y2 = roomHeight - taw / 2f;
                    }
                    floor.createAisle(x, y1, x, y2);
                }

                for (int j = 1; j < numCols; j++) {
                    float y = roomTop + j * taw;
                    floor.createAisle(roomLeft + tad, y, roomLeft + numRows * tad, y);
                }

                if (showTeams) {
                    int roomTeamCount = 0;
                    int maxInRoom = Integer.MAX_VALUE;
                    if (teamsPerRoom.containsKey(room)) {
                        maxInRoom = teamsPerRoom.get(room);
                    }
                    for (int r = 0; r < numRows; r++) {
                        for (int c = 0; c < numCols; c++) {
                            float x = roomLeft + tad - magicXDiff + (r * tad);
                            float y = roomTop + taw / 2f + ((numCols - c - 1) * taw);
                            SkippedTeam s = new SkippedTeam(room, r + 1, c + 1);
                            if (skippedTeams.contains(s) || roomTeamCount >= maxInRoom) {
                                floor.createTeam(-1, x, y, FloorMap.E);
                            } else {
                                floor.createTeam(teamId, x, y, FloorMap.E);
                                teamId++;
                                roomTeamCount++;
                            }
                        }
                    }
                }

                room++;
            }

            // Bottom rooms
            for (int i = 0; i < numRooms / 2; i++) {
                float roomLeft = 1 + (numRooms / 2f - 1 - i) * (roomWidth + innerRoomSpace);
                for (int j = 0; j < numRows; j++) {
                    float x = roomLeft + j * tad;
                    float y1;
                    float y2 = bottomRoomsStart + roomHeight - taw / 2f;
                    if (j == 0 || j == numRows - 1) {
                        y1 = bottomRoomsStart;
                    } else {
                        y1 = bottomRoomsStart + taw / 2f;
                    }
                    floor.createAisle(x, y1, x, y2);
                }

                for (int j = 1; j < numCols; j++) {
                    float y = bottomRoomsStart + j * taw;
                    floor.createAisle(roomLeft, y, roomLeft + (numRows - 1) * tad, y);
                }

                if (showTeams) {
                    int roomTeamCount = 0;
                    int maxInRoom = Integer.MAX_VALUE;
                    if (teamsPerRoom.containsKey(room)) {
                        maxInRoom = teamsPerRoom.get(room);
                    }
                    for (int r = numRows - 1; r >= 0; r--) {
                        for (int c = 0; c < numCols; c++) {
                            float x = roomLeft + tad - magicXDiff2 + (r * tad);
                            float y = bottomRoomsStart + taw / 2f + (c * taw);
                            SkippedTeam s = new SkippedTeam(room, r + 1, c + 1);
                            if (skippedTeams.contains(s) || roomTeamCount >= maxInRoom) {
                                floor.createTeam(-1, x, y, FloorMap.W);
                            } else {
                                floor.createTeam(teamId, x, y, FloorMap.W);
                                teamId++;
                                roomTeamCount++;
                            }
                        }
                    }
                }

                room++;
            }

//
            floor.writeCSV(System.out);
//
//            Trace.trace(Trace.USER, "------------------");
//
            long time = System.currentTimeMillis();
            Path path1 = floor.getPath(floor.getTeam(45), floor.getTeam(38));
//            Path path2 = floor.getPath(t1, floor.getPrinter());

            Trace.trace(Trace.USER, "Time: " + (System.currentTimeMillis() - time));

//            show(floor, -1, true, path1, path2);
            show(floor, 37, true, path1);
        } catch (Exception e) {
            Trace.trace(Trace.ERROR, "Error generating floor map", e);
        }
    }
}